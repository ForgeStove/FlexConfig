package io.github.forgestove.flexconfig;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.api.*;
import io.github.forgestove.flexconfig.client.gui.factory.EntryKeybind;
import io.github.forgestove.flexconfig.platform.Platform;
import io.github.forgestove.flexconfig.tree.RootConfigNode;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.*;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.*;
/** 文件路径由 {@link ConfigNaming} 解析；SERVER 型要进世界才能定位存档目录，故构造期不读盘，其余类型在构造期读取。 */
public final class ConfigHandler<C, V> {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Map<String, Runnable> triggerActions = new LinkedHashMap<>();
	private final Class<C> configClass;
	private final RootConfigNode<C, V> configTree;
	private final ConfigSerializer<C> serializer;
	private final C savedConfig;
	private final C activeConfig;
	private final String modId;
	private final ConfigType type;
	private final String fileName;
	// ---- 全局快捷键系统 ----
	private final Map<String, String> triggerKeybinds = new LinkedHashMap<>();
	/** 已解析的磁盘路径；SERVER 型在进世界前为 {@code null}。 */
	@Nullable private Path resolvedPath;
	private ConfigHandler(Builder<C, V> builder) {
		configClass = builder.configClass;
		modId = ConfigRegistry.getModId(configClass);
		type = builder.type;
		fileName = builder.fileName;
		serializer = builder.serializerBuilder.build();
		configTree = RootConfigNode.create(newInstance(), modId, fileName, type);
		savedConfig = newInstance();
		activeConfig = newInstance();
		if (loadsAtRegistration()) loadFromDisk(null);
	}
	public static <C, V> @NotNull Builder<C, V> builder(Class<C> configClass) {
		return new Builder<>(configClass);
	}
	private @NotNull C newInstance() {
		try {
			return configClass.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Failed to create config instance", e);
		}
	}
	private boolean loadsAtRegistration() {
		return switch (type) {
			case SERVER -> false;
			case CLIENT -> Platform.get().isClient();
			case COMMON -> true;
		};
	}
	public void loadFromDisk(@Nullable MinecraftServer server) {
		resolvedPath = ConfigNaming.resolve(modId, type, fileName, server);
		serializer.setTargetPath(resolvedPath);
		reloadAndCopy();
	}
	/** 重读磁盘并覆盖 saved 与 active，连同锁值与已下发的服务端值一并撤销。 */
	public void reloadAndCopy() {
		if (resolvedPath == null) return;
		CommentedConfig toml;
		try {
			toml = serializer.read();
		} catch (SerializationException e) {
			LOGGER.error("Failed to read configuration, keeping current values", e);
			return;
		}
		var fresh = toml == null ? newInstance() : deserialize(toml);
		configTree.copy(fresh, savedConfig);
		// 运行时按需同步：待生效的延迟项保持旧值（重载不该让它们提前生效）
		configTree.copyForRuntime(fresh, activeConfig);
		triggerKeybinds.clear();
		if (toml != null) triggerKeybinds.putAll(ConfigSerializer.readKeybinds(toml));
		// 刚读过盘，把修改时间记为基线，免得热重载把自己的这次读取当成外部改动
		ConfigWatchService.markCurrent(new FileKey(modId, fileName));
	}
	private C deserialize(CommentedConfig toml) {
		try {
			return serializer.deserializeFrom(toml);
		} catch (SerializationException e) {
			LOGGER.error("Failed to load configuration, using defaults", e);
			return newInstance();
		}
	}
	/** 用服务端下发的整份内容覆盖 saved 与 active；仅改内存，不落盘也不读本地文件。 */
	public void applySynced(CommentedConfig toml) {
		var fresh = deserialize(toml);
		configTree.copy(fresh, savedConfig);
		configTree.copy(fresh, activeConfig);
	}
	/** SERVER 型在世界停止时调用：恢复默认值并丢弃已解析路径。 */
	public void unload() {
		resolvedPath = null;
		var defaults = newInstance();
		configTree.copy(defaults, savedConfig);
		configTree.copy(defaults, activeConfig);
		triggerKeybinds.clear();
	}
	public RootConfigNode<C, V> getConfigTree() {
		return configTree;
	}
	public C getConfig() {
		return activeConfig;
	}
	public C getSavedConfig() {
		return savedConfig;
	}
	public ConfigSerializer<C> getSerializer() {
		return serializer;
	}
	public String getModId() {
		return modId;
	}
	public ConfigType getType() {
		return type;
	}
	public String getFileName() {
		return fileName;
	}
	public @Nullable Path getPath() {
		return resolvedPath;
	}
	@SuppressWarnings("unchecked")
	public ConfigHandler<Object, Object> cast() {
		return (ConfigHandler<Object, Object>) this;
	}
	// ========== 全局快捷键系统 ==========
	/** 值、本地化注释与快捷键段一次性写出，采用临时文件加原子替换，写入失败时原文件保持完整。 */
	public void save(C config) {
		// 全量写入文件模型 → 补上延迟生效项的新值（运行时仍守旧值）→ 运行时仅接收即时生效的改动
		configTree.copy(config, savedConfig);
		configTree.flushPending(savedConfig);
		configTree.copyForRuntime(config, activeConfig);
		if (resolvedPath == null) return;   // 路径未解析（SERVER 未进世界）时没有落盘目标
		try {
			serializer.serialize(savedConfig, triggerKeybinds);
		} catch (SerializationException e) {
			LOGGER.error("Failed to save configuration", e);
		}
		// 刚写过盘，把修改时间记为基线，免得热重载把自己的这次写入当成外部改动
		ConfigWatchService.markCurrent(new FileKey(modId, fileName));
	}
	/** 退出世界或服务端停止时调用，让 WORLD 型延迟生效的改动写入活动配置。 */
	public void applyWorldRestart() {
		configTree.applyRestart(savedConfig, activeConfig, RestartType.WORLD);
	}
	/** {@code serialized} 为空时清除绑定；{@code action} 为 {@code null} 时保留已有动作。 */
	public void setTriggerKeybind(String path, @Nullable String serialized, @Nullable Runnable action) {
		if (serialized == null || serialized.isEmpty()) triggerKeybinds.remove(path);
		else triggerKeybinds.put(path, serialized);
		if (action != null) triggerActions.put(path, action);
	}
	public @Nullable String getTriggerKeybind(String path) {
		return triggerKeybinds.get(path);
	}
	public Map<String, String> getAllTriggerKeybinds() {
		return Collections.unmodifiableMap(triggerKeybinds);
	}
	public void tickTriggerKeybinds(long window, Map<String, Boolean> previousStates) {
		triggerKeybinds.forEach((path, value) -> {
			var keybind = EntryKeybind.deserialize(value);
			if (keybind.isUnbound()) return;
			var isPressed = EntryKeybind.isKeyPressed(window, keybind);
			var wasPressed = previousStates.getOrDefault(path, false);
			previousStates.put(path, isPressed);
			if (isPressed && !wasPressed) {
				var action = triggerActions.get(path);
				if (action != null) action.run();
			}
		});
	}
	/** 以快照整体覆盖当前快捷键绑定，用于取消编辑时回撤。 */
	public void restoreTriggerKeybinds(Map<String, String> snapshot) {
		triggerKeybinds.clear();
		triggerKeybinds.putAll(snapshot);
	}
	public static final class Builder<C, V> {
		private final Class<C> configClass;
		private final ConfigSerializer.Builder<C> serializerBuilder;
		private ConfigType type = ConfigType.CLIENT;
		private String fileName;
		private Builder(Class<C> configClass) {
			this.configClass = configClass;
			serializerBuilder = ConfigSerializer.builder(configClass);
		}
		public Builder<C, V> type(ConfigType type) {
			this.type = type;
			return this;
		}
		public Builder<C, V> fileName(String fileName) {
			this.fileName = fileName;
			return this;
		}
		public @NotNull ConfigHandler<C, V> build() {
			return new ConfigHandler<>(this);
		}
	}
}
