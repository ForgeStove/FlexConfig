package io.github.forgestove.flexconfig;
import io.github.forgestove.flexconfig.api.*;
import io.github.forgestove.flexconfig.client.FlexConfigScreenFactory;
import io.github.forgestove.flexconfig.platform.Platform;
import io.github.forgestove.flexconfig.tree.*;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
/** 一个模组可按 {@link ConfigType} 注册多份配置，各自对应一个文件。 */
public final class ConfigRegistry {
	private static final Map<String, EnumMap<ConfigType, ConfigHandler<?, ?>>> SETS = new ConcurrentHashMap<>();
	/** 供锁定、下发与热重载按文件精确定位。 */
	private static final Map<FileKey, ConfigHandler<?, ?>> BY_FILE = new ConcurrentHashMap<>();
	/** 「主」配置的优先顺序，供按 modId 取配置的调用点（GUI 入口、旧锁代码）使用。 */
	private static final List<ConfigType> PRIMARY_ORDER = List.of(ConfigType.CLIENT, ConfigType.COMMON, ConfigType.SERVER);
	/** 同一 (模组 ID, 类型) 仅构建一次；同类型重复注册不同配置类会抛异常。 */
	@SuppressWarnings({"unused", "UnusedReturnValue"})
	public static <C> C init(Class<C> clazz) {
		var annotation = requireAnnotation(clazz);
		var modId = annotation.value();
		var type = annotation.type();
		var fileName = ConfigNaming.fileName(modId, type, annotation.fileName());
		var set = SETS.computeIfAbsent(
			modId, id -> {
				initConfigScreenForClient(id);
				return new EnumMap<>(ConfigType.class);
			}
		);
		var handler = set.computeIfAbsent(
			type, ignored -> {
				var built = ConfigHandler.builder(clazz).type(type).fileName(fileName).build();
				BY_FILE.put(new FileKey(modId, fileName), built);
				return built;
			}
		);
		var config = handler.getConfig();
		if (clazz.isInstance(config)) return clazz.cast(config);
		throw new IllegalStateException("Config handler for %s/%s returned %s, expected %s".formatted(
			modId,
			type,
			config.getClass().getName(),
			clazz.getName()
		));
	}
	public static @NotNull Config requireAnnotation(Class<?> configClass) {
		var annotation = configClass.getAnnotation(Config.class);
		if (annotation == null) throw new IllegalStateException("Config class %s is missing @Config".formatted(configClass.getName()));
		if (annotation.value().isBlank()) throw new IllegalStateException("Mod id must be provided before building ConfigHandler");
		return annotation;
	}
	private static void initConfigScreenForClient(String modId) {
		if (!Platform.get().isClient()) return;
		Platform.get().registerConfigScreen(modId, () -> FlexConfigScreenFactory.createConfigScreen(modId));
	}
	public static String getModId(@NotNull Class<?> configClass) {
		return requireAnnotation(configClass).value();
	}
	// ========== 按 modId（主配置）访问 ==========
	public static List<ConfigHandler<?, ?>> getHandlers(String modId) {
		var set = SETS.get(modId);
		if (set == null) return List.of();
		var handlers = new ArrayList<ConfigHandler<?, ?>>(set.size());
		for (var type : PRIMARY_ORDER) {
			var handler = set.get(type);
			if (handler != null) handlers.add(handler);
		}
		return List.copyOf(handlers);
	}
	public static Object getActiveConfig(FileKey key) {
		return BY_FILE.containsKey(key) ? getHandler(key).getConfig() : null;
	}
	// ========== 按文件精确定位 ==========
	public static @NotNull ConfigHandler<?, ?> getHandler(FileKey key) {
		var handler = BY_FILE.get(key);
		if (handler == null) throw new IllegalStateException("Config handler for '%s' is not initialized.".formatted(key));
		return handler;
	}
	public static RootConfigNode<Object, Object> getRootConfigNode(FileKey key) {
		return getHandler(key).cast().getConfigTree();
	}
	public static void resetToSaved(FileKey key) {
		getHandler(key).cast().reloadAndCopy();
	}
	/** 仅作用于活动配置，不修改 savedConfig，因此不会污染 TOML 文件。 */
	public static void applyLockedValue(FileKey key, ValueConfigNode<Object, Object> node, Object parsed) {
		node.setActiveValue(getHandler(key).cast().getConfig(), parsed);
	}
	/** 退出世界或服务端停止时调用，让各份配置的 WORLD 型延迟改动写入活动配置。 */
	public static void applyWorldRestart() {
		BY_FILE.values().forEach(ConfigHandler::applyWorldRestart);
	}
	// ========== 生命周期 ==========
	/** 世界即将开始：解析并加载 SERVER 型配置（存档优先，回退全局 config）。 */
	public static void loadServerConfigs(MinecraftServer server) {
		BY_FILE.values().forEach(handler -> {
			if (handler.getType() == ConfigType.SERVER) handler.loadFromDisk(server);
		});
	}
	public static void unloadServerConfigs() {
		BY_FILE.values().forEach(handler -> {
			if (handler.getType() == ConfigType.SERVER) handler.unload();
		});
	}
	// ========== 查询 ==========
	/** 锁的语义是服务端强制客户端的本地配置，故可锁的仅是 {@link ConfigType#CLIENT}（客户端自己读的那份）。 */
	public static boolean isLockable(FileKey key) {
		var handler = BY_FILE.get(key);
		return handler != null && handler.getType() == ConfigType.CLIENT;
	}
	public static Set<FileKey> getRegisteredFileKeys() {
		return Set.copyOf(BY_FILE.keySet());
	}
}
