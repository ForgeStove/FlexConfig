package io.github.forgestove.flexconfig.foundation;
import io.github.forgestove.flexconfig.api.*;
import io.github.forgestove.flexconfig.api.annotation.Config;
import io.github.forgestove.flexconfig.api.client.FlexConfigScreenFactory;
import io.github.forgestove.flexconfig.api.registration.ConfigHandler;
import io.github.forgestove.flexconfig.foundation.tree.*;
import io.github.forgestove.flexconfig.platform.*;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.*;

import java.util.*;
/** 一个模组可按 {@link ConfigType} 注册多份配置，各自对应一个文件。 */
public class ConfigStore {
	private static final Object LOCK = new Object();
	private static final List<ConfigType> PRIMARY_ORDER = List.of(ConfigType.CLIENT, ConfigType.COMMON, ConfigType.SERVER);
	/** 注册成功后整体替换，读路径永远看到一致的一组索引。 */
	private static volatile Snapshot SNAPSHOT = Snapshot.EMPTY;
	/** 由 {@code ConfigHandler.Builder#build()} 调用；同一 (模组 ID, 类型)、同一 Class、同一文件均只能注册一次。 */
	public static <C, V> void register(ConfigHandler<C, V> handler) {
		var modId = handler.getModId();
		var type = handler.getType();
		var fileName = handler.getFileName();
		var configClass = handler.getConfigClass();
		boolean firstForMod;
		var fileKey = new FileKey(modId, fileName);
		synchronized (LOCK) {
			var cur = SNAPSHOT;
			var oldMod = cur.byMod().get(modId);
			var dupType = oldMod == null ? null : oldMod.get(type);
			if (dupType != null) throw duplicate("type", "%s/%s".formatted(modId, type), dupType);
			var dupFile = cur.byFile().get(fileKey);
			if (dupFile != null) throw duplicate("file", fileKey.toString(), dupFile);
			var dupClass = getByClass(cur.byClass(), configClass);
			if (dupClass != null) throw duplicate("class", configClass.getName(), dupClass);
			var byFile = new HashMap<>(cur.byFile());
			byFile.put(fileKey, handler);
			var byClass = new HashMap<>(cur.byClass());
			putByClass(byClass, configClass, handler);
			var byMod = new HashMap<>(cur.byMod());
			var modMap = new EnumMap<ConfigType, ConfigHandler<?, ?>>(ConfigType.class);
			if (oldMod != null) modMap.putAll(oldMod);
			modMap.put(type, handler);
			byMod.put(modId, Collections.unmodifiableMap(modMap));
			SNAPSHOT = new Snapshot(Map.copyOf(byFile), Map.copyOf(byClass), Map.copyOf(byMod));
			firstForMod = oldMod == null;
		}
		if (firstForMod) ensureScreenRegistered(modId);
	}
	private static IllegalStateException duplicate(String dimension, String id, ConfigHandler<?, ?> existing) {
		return new IllegalStateException("Config %s '%s' is already registered by %s".formatted(
			dimension,
			id,
			existing.getConfigClass().getName()
		));
	}
	private static void ensureScreenRegistered(String modId) {
		if (!FlexConfigServices.PLATFORM.isClient()) return;
		FlexConfigClientServices.CLIENT_UI.registerConfigScreen(modId, () -> FlexConfigScreenFactory.createConfigScreen(modId));
	}
	public static @NotNull Config requireAnnotation(Class<?> configClass) {
		var annotation = configClass.getAnnotation(Config.class);
		if (annotation == null) throw new IllegalStateException("Config class %s is missing @Config".formatted(configClass.getName()));
		if (annotation.value().isBlank()) throw new IllegalStateException("Mod id must be provided before building ConfigHandler");
		return annotation;
	}
	public static String getModId(@NotNull Class<?> configClass) {
		return requireAnnotation(configClass).value();
	}
	// ========== 按 modId（主配置）访问 ==========
	/** 按 {@link #PRIMARY_ORDER} 顺序返回该 mod 已注册的主配置 handler；未注册返回空列表。 */
	public static List<ConfigHandler<?, ?>> getHandlers(String modId) {
		var modMap = SNAPSHOT.byMod().get(modId);
		if (modMap == null) return List.of();
		var handlers = new ArrayList<ConfigHandler<?, ?>>(PRIMARY_ORDER.size());
		for (var type : PRIMARY_ORDER) {
			var handler = modMap.get(type);
			if (handler != null) handlers.add(handler);
		}
		return List.copyOf(handlers);
	}
	public static @Nullable Object getActiveConfig(FileKey key) {
		var handler = findByFile(key);
		return handler == null ? null : handler.getConfig();
	}
	// ========== 按 Class / 文件精确定位 ==========
	@SuppressWarnings("unused")
	public static <C> @NotNull ConfigHandler<C, ?> getHandler(Class<C> configClass) {
		var handler = getByClass(SNAPSHOT.byClass(), configClass);
		if (handler == null)
			throw new IllegalStateException("Config handler for '%s' is not initialized.".formatted(configClass.getName()));
		return handler;
	}
	public static @NotNull ConfigHandler<?, ?> getHandler(FileKey key) {
		var handler = findByFile(key);
		if (handler == null) throw new IllegalStateException("Config handler for '%s' is not initialized.".formatted(key));
		return handler;
	}
	@Internal
	public static RootConfigNode<Object, Object> getRootConfigNode(FileKey key) {
		return getHandler(key).cast().getConfigTree();
	}
	@Internal
	public static void resetToSaved(FileKey key) {
		getHandler(key).cast().reloadAndCopy();
	}
	/** 仅作用于活动配置，不修改 savedConfig，因此不会污染 TOML 文件。 */
	@Internal
	public static void applyLockedValue(FileKey key, ValueConfigNode<Object, Object> node, Object parsed) {
		node.setActiveValue(getHandler(key).cast().getConfig(), parsed);
	}
	/** 退出世界或服务端停止时调用，让各份配置的 WORLD 型延迟改动写入活动配置。 */
	public static void applyWorldRestart() {
		for (var handler : allHandlers()) handler.applyWorldRestart();
	}
	// ========== 生命周期 ==========
	/** 世界即将开始：解析并加载 SERVER 型配置（存档优先，回退全局 config）。 */
	public static void loadServerConfigs(MinecraftServer server) {
		for (var handler : allHandlers()) if (handler.getType() == ConfigType.SERVER) handler.loadFromDisk(server);
	}
	public static void unloadServerConfigs() {
		for (var handler : allHandlers()) if (handler.getType() == ConfigType.SERVER) handler.unload();
	}
	// ========== 查询 ==========
	/** 锁的语义是服务端强制客户端的本地配置；只有 CLIENT 型且注册时显式开启 {@code lockable} 才生效。 */
	@Internal
	public static boolean isLockable(FileKey key) {
		var handler = findByFile(key);
		return handler != null && handler.getType() == ConfigType.CLIENT && handler.isLockable();
	}
	@Internal
	public static Set<FileKey> getRegisteredFileKeys() {
		return SNAPSHOT.byFile().keySet();
	}
	private static @Nullable ConfigHandler<?, ?> findByFile(FileKey key) {
		return SNAPSHOT.byFile().get(key);
	}
	private static Collection<ConfigHandler<?, ?>> allHandlers() {
		return SNAPSHOT.byFile().values();
	}
	/** 把 key 的配置类与 value 的类型参数绑在一起，避免向通配符表写错类型。 */
	private static <C, V> void putByClass(Map<Class<?>, ConfigHandler<?, ?>> target, Class<C> configClass, ConfigHandler<C, V> handler) {
		target.put(configClass, handler);
	}
	@SuppressWarnings("unchecked")
	private static <C> @Nullable ConfigHandler<C, ?> getByClass(Map<Class<?>, ConfigHandler<?, ?>> source, Class<C> configClass) {
		return (ConfigHandler<C, ?>) source.get(configClass);
	}
	/** 按文件、Class、modId 建立索引；{@code byMod} 的内层 map 保留枚举声明顺序。 */
	private record Snapshot(
		Map<FileKey, ConfigHandler<?, ?>> byFile,
		Map<Class<?>, ConfigHandler<?, ?>> byClass,
		Map<String, Map<ConfigType, ConfigHandler<?, ?>>> byMod
	) {
		static final Snapshot EMPTY = new Snapshot(Map.of(), Map.of(), Map.of());
	}
}
