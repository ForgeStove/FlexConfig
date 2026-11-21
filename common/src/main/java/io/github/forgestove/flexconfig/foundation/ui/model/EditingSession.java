package io.github.forgestove.flexconfig.foundation.ui.model;
import io.github.forgestove.flexconfig.api.*;
import io.github.forgestove.flexconfig.api.registration.ConfigHandler;
import io.github.forgestove.flexconfig.foundation.ConfigStore;
import io.github.forgestove.flexconfig.foundation.client.ClientLockManager;
import io.github.forgestove.flexconfig.foundation.tree.*;
import io.github.forgestove.flexconfig.foundation.ui.core.UiText;
import io.github.forgestove.flexconfig.foundation.ui.factory.Keybinds;
import org.jetbrains.annotations.Nullable;

import java.util.*;
/**
 * 一次配置编辑的会话状态。持有活动配置、按键绑定分类和撤销快照，负责校验、保存和重置，
 * 界面只调用这些操作。
 */
public class EditingSession<C, V> {
	private final ConfigHandler<C, V> handler;
	private final RootConfigNode root;
	private final C config;
	private @Nullable CategoryConfigNode keybindCategory;
	private Map<String, String> savedTriggerKeybinds = Map.of();
	/** 首次打开才重置编辑值；从子界面返回时保留未保存的修改。 */
	private boolean initialized;
	public EditingSession(ConfigHandler<C, V> handler) {
		this.handler = handler;
		root = handler.getConfigTree();
		config = handler.getConfig();
	}
	public ConfigHandler<C, V> handler() {
		return handler;
	}
	public RootConfigNode root() {
		return root;
	}
	public C config() {
		return config;
	}
	public @Nullable CategoryConfigNode keybindCategory() {
		return keybindCategory;
	}
	public void begin() {
		if (initialized) return;
		initialized = true;
		ClientLockManager.clearPendingLocks();
		captureTriggerKeybinds();
		root.resetToActive(config);
	}
	public void buildKeybindCategory() {
		var built = Keybinds.support().buildCategory(root.modId());
		if (built == null) {
			keybindCategory = null;
			return;
		}
		built.resetToActive(config);
		keybindCategory = built;
	}
	public List<CategoryConfigNode> categories() {
		var list = new ArrayList<>(root.categories());
		if (keybindCategory != null) list.add(keybindCategory);
		return list;
	}
	public boolean editable() {
		return handler.getType() != ConfigType.SERVER || Keybinds.support().singleplayerServer();
	}
	public boolean lockable() {
		return ConfigStore.isLockable(new FileKey(root.modId(), root.fileName()));
	}
	public @Nullable UiText validate() {
		var rootError = root.validate(config);
		if (rootError != null) return rootError;
		return keybindCategory == null ? null : keybindCategory.validate(config);
	}
	public void writeEditingToConfig() {
		root.writeEditingToConfig(config);
		if (keybindCategory != null) keybindCategory.writeEditingToConfig(config);
	}
	public @Nullable RestartType restartType() {
		return root.restartType(config);
	}
	/** 保存前把编辑值写入配置与按键映射；写盘失败时返回 false，调用方不关闭界面。 */
	public boolean save() {
		writeEditingToConfig();
		Keybinds.support().saveOptions();
		if (!handler.save(config)) return false;
		ClientLockManager.flushPendingLocks();
		captureTriggerKeybinds();
		return true;
	}
	public void saveByKeybind() {
		if (editable()) handler.save(config);
	}
	public void resetAll() {
		root.resetToActive(config);
		if (keybindCategory != null) keybindCategory.resetToActive(config);
		handler.restoreTriggerKeybinds(savedTriggerKeybinds);
		ClientLockManager.clearPendingLocks();
	}
	public void restoreTriggerKeybinds() {
		handler.restoreTriggerKeybinds(savedTriggerKeybinds);
	}
	public boolean isActiveValue() {
		if (ClientLockManager.hasPendingLocks()) return false;
		if (!root.isActiveValue(config)) return false;
		if (keybindCategory != null && !keybindCategory.isActiveValue(config)) return false;
		return savedTriggerKeybinds.equals(handler.getAllTriggerKeybinds());
	}
	private void captureTriggerKeybinds() {
		savedTriggerKeybinds = new LinkedHashMap<>(handler.getAllTriggerKeybinds());
	}
}
