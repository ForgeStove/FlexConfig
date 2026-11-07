package io.github.forgestove.flexconfig.api.registration;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.api.*;
import io.github.forgestove.flexconfig.api.menu.*;
import io.github.forgestove.flexconfig.foundation.*;
import io.github.forgestove.flexconfig.foundation.tree.RootConfigNode;
import io.github.forgestove.flexconfig.foundation.ui.factory.EntryKeybind;
import io.github.forgestove.flexconfig.platform.FlexConfigServices;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.*;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
@SuppressWarnings("unused")
public class ConfigHandler<C, V> {
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
	private final boolean lockable;
	private final MenuOptions menuOptions;
	private final Accent accent;
	// ---- 全局快捷键系统 ----
	private final Map<String, String> triggerKeybinds = new LinkedHashMap<>();
	private final List<ConfigSaveListener<C>> saveListeners = new ArrayList<>();
	private final List<ConfigLoadListener<C>> loadListeners = new ArrayList<>();
	private final List<ConfigValidator<C>> validators = new ArrayList<>();
	/** 已解析的磁盘路径；SERVER 型在进世界前为 {@code null}。 */
	@Nullable private Path resolvedPath;
	private ConfigHandler(Builder<C, V> builder) {
		configClass = builder.configClass;
		modId = ConfigStore.getModId(configClass);
		type = builder.type;
		fileName = builder.fileName;
		lockable = builder.lockable;
		menuOptions = buildMenuOptions(builder);
		accent = builder.accent;
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
			case CLIENT -> FlexConfigServices.PLATFORM.isClient();
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
		var fresh = acceptLoaded(toml == null ? newInstance() : deserialize(toml));
		configTree.copy(fresh, savedConfig);
		// 重载时延迟生效的项保持旧值，避免它们提前生效
		configTree.copyForRuntime(fresh, activeConfig);
		triggerKeybinds.clear();
		if (toml != null) triggerKeybinds.putAll(ConfigSerializer.readKeybinds(toml));
		// 读取文件后记录修改时间，避免热重载把这次读取当成外部改动
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
	/** 用服务端下发的整份内容覆盖 saved 与 active；只修改内存，不写入文件，也不读取本地文件。 */
	public void applySynced(CommentedConfig toml) {
		var fresh = acceptLoaded(deserialize(toml));
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
	@Internal
	public RootConfigNode<C, V> getConfigTree() {
		return configTree;
	}
	public C getConfig() {
		return activeConfig;
	}
	public C getSavedConfig() {
		return savedConfig;
	}
	public Class<C> getConfigClass() {
		return configClass;
	}
	@Internal
	public ConfigSerializer<C> getSerializer() {
		return serializer;
	}
	public String getModId() {
		return modId;
	}
	public ConfigType getType() {
		return type;
	}
	public boolean isLockable() {
		return lockable;
	}
	public MenuOptions getMenuOptions() {
		return menuOptions;
	}
	public Accent getAccent() {
		return accent;
	}
	private @NotNull MenuOptions buildMenuOptions(Builder<C, V> builder) {
		return builder.menuIcon == null ? MenuOptions.NONE : new MenuOptions(builder.menuIcon);
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
	public boolean save(C config) {
		var message = validateConfig(config);
		if (message != null) {
			LOGGER.warn("Configuration '{}' failed validation: {}", fileName, message);
			return false;
		}
		for (var listener : saveListeners)
			if (!listener.onSave(config)) {
				LOGGER.warn("Configuration '{}' save canceled by listener", fileName);
				return false;
			}
		// 全量写入文件模型 → 补上延迟生效项的新值（运行时仍守旧值）→ 运行时仅接收即时生效的改动
		configTree.copy(config, savedConfig);
		configTree.flushPending(savedConfig);
		configTree.copyForRuntime(config, activeConfig);
		if (resolvedPath == null) return true;   // 路径未解析（SERVER 未进世界）时没有落盘目标
		try {
			serializer.serialize(savedConfig, triggerKeybinds);
		} catch (SerializationException e) {
			LOGGER.error("Failed to save configuration", e);
		}
		// 写入文件后记录修改时间，避免热重载把这次写入当成外部改动
		ConfigWatchService.markCurrent(new FileKey(modId, fileName));
		return true;
	}
	public void registerSaveListener(ConfigSaveListener<C> listener) {
		saveListeners.add(listener);
	}
	public void registerLoadListener(ConfigLoadListener<C> listener) {
		loadListeners.add(listener);
	}
	public void registerValidator(ConfigValidator<C> validator) {
		validators.add(validator);
	}
	private @Nullable String validateConfig(C config) {
		for (var validator : validators) {
			var message = validator.validate(config);
			if (message != null) return message;
		}
		return null;
	}
	private C acceptLoaded(C fresh) {
		for (var listener : loadListeners)
			if (!listener.onLoad(fresh)) {
				LOGGER.warn("Configuration '{}' load rejected by listener, using defaults", fileName);
				return newInstance();
			}
		var message = validateConfig(fresh);
		if (message != null) {
			LOGGER.warn("Configuration '{}' failed validation: {}, using defaults", fileName, message);
			return newInstance();
		}
		return fresh;
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
	/** 删除列表元素后，同级后续元素的快捷键路径整体前移一位；被删元素自身的绑定一并丢弃。 */
	public void shiftElementKeybinds(String parentPath, int removedIndex) {
		var prefix = parentPath + "[";
		var shiftedBindings = new ArrayList<Entry<String, String>>();
		var shiftedActions = new LinkedHashMap<String, Runnable>();
		var iterator = triggerKeybinds.entrySet().iterator();
		while (iterator.hasNext()) {
			var entry = iterator.next();
			var path = entry.getKey();
			if (!path.startsWith(prefix)) continue;
			var close = path.indexOf(']', prefix.length());
			if (close < 0) continue;
			int index;
			try {
				index = Integer.parseInt(path.substring(prefix.length(), close));
			} catch (NumberFormatException e) {
				continue;
			}
			if (index < removedIndex) continue;
			iterator.remove();
			var action = triggerActions.remove(path);
			if (index == removedIndex) continue;
			var shifted = "%s[%d]%s".formatted(parentPath, index - 1, path.substring(close + 1));
			shiftedBindings.add(Map.entry(shifted, entry.getValue()));
			if (action != null) shiftedActions.put(shifted, action);
		}
		shiftedBindings.forEach(entry -> triggerKeybinds.put(entry.getKey(), entry.getValue()));
		triggerActions.putAll(shiftedActions);
	}
	/** 列表整体重置时丢弃挂在其元素上的全部快捷键。 */
	public void clearElementKeybinds(String parentPath) {
		var prefix = parentPath + "[";
		triggerKeybinds.keySet().removeIf(path -> path.startsWith(prefix));
		triggerActions.keySet().removeIf(path -> path.startsWith(prefix));
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
	/** 用快照整体覆盖当前快捷键绑定，取消编辑时回撤到快照。 */
	public void restoreTriggerKeybinds(Map<String, String> snapshot) {
		triggerKeybinds.clear();
		triggerKeybinds.putAll(snapshot);
	}
	public static class Builder<C, V> {
		private final Class<C> configClass;
		private final ConfigSerializer.Builder<C> serializerBuilder;
		private ConfigType type;
		private String fileName;
		private boolean lockable;
		private Accent accent = Accent.DEFAULT;
		private @Nullable ResourceLocation menuIcon;
		private Builder(Class<C> configClass) {
			this.configClass = configClass;
			serializerBuilder = ConfigSerializer.builder(configClass);
			type = ConfigStore.requireAnnotation(configClass).type();
		}
		public Builder<C, V> type(ConfigType type) {
			this.type = type;
			return this;
		}
		public Builder<C, V> fileName(String fileName) {
			this.fileName = fileName;
			return this;
		}
		public Builder<C, V> lockable(boolean lockable) {
			this.lockable = lockable;
			return this;
		}
		public Builder<C, V> theme(int accent) {
			this.accent = Accent.of(accent);
			return this;
		}
		public Builder<C, V> menuIcon(@Nullable ResourceLocation icon) {
			menuIcon = icon;
			return this;
		}
		/** 构建并注册到 {@link ConfigStore}；同一 (模组, 类型) 只能注册一次。 */
		public @NotNull ConfigHandler<C, V> build() {
			if (fileName == null || fileName.isBlank()) fileName = ConfigNaming.fileName(ConfigStore.getModId(configClass), type, "");
			var handler = new ConfigHandler<>(this);
			ConfigStore.register(handler);
			return handler;
		}
	}
}
