package io.github.forgestove.flexconfig.client.gui;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.flexconfig.*;
import io.github.forgestove.flexconfig.api.*;
import io.github.forgestove.flexconfig.client.*;
import io.github.forgestove.flexconfig.client.gui.api.CaptureHandler;
import io.github.forgestove.flexconfig.client.gui.components.*;
import io.github.forgestove.flexconfig.client.gui.entry.ConfigEntry;
import io.github.forgestove.flexconfig.client.gui.popup.OreConfirmPopupScreen;
import io.github.forgestove.flexconfig.tree.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.layouts.*;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.*;
import org.lwjgl.glfw.GLFW;

import java.util.*;
public final class ConfigScreen<C, V> extends Screen {
	public static CategoryConfigNode<?> lastSelectedCategory;
	public final RootConfigNode<C, V> root;
	private final C config;
	private final ConfigHandler<C, V> handler;
	private final Screen parent;
	private final TabManager tabManager;
	private final List<ConfigCategoryTab<C, V>> tabs;
	private final HeaderAndFooterLayout layout;
	private CategoryConfigNode<C> keybindCategory;
	private OreTabNavigationBar tabNavigationBar;
	private OreButton cancelButton, saveButton;
	private CaptureHandler capturingEntry;
	private Map<String, String> savedTriggerKeybinds = Map.of();
	public ConfigScreen(ConfigHandler<C, V> handler) {
		super(handler.getConfigTree().getTitle());
		root = handler.getConfigTree();
		config = handler.getConfig();
		this.handler = handler;
		parent = ClientUtil.mc.screen;
		tabs = new ArrayList<>();
		layout = new HeaderAndFooterLayout(this);
		tabManager = new TabManager(this::addRenderableWidget, this::removeWidget);
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (capturingEntry != null && capturingEntry.handleCaptureKey(keyCode)) return true;
		if (tabNavigationBar != null && tabNavigationBar.keyPressed(keyCode)) {
			cacheTabIndex();
			return true;
		}
		if (super.keyPressed(keyCode, scanCode, modifiers)) return true;   // 聚焦的输入框先吃退格
		if (keyCode == GLFW.GLFW_KEY_BACKSPACE) onClose();
		return false;
	}
	private void cacheTabIndex() {
		if (tabManager.getCurrentTab() instanceof ConfigCategoryTab<?, ?> tab) lastSelectedCategory = tab.node;
	}
	@Override
	public void onClose() {
		var mc = ClientUtil.mc;
		if (isActiveValue()) {
			mc.setScreen(parent);
			return;
		}
		mc.setScreen(new OreConfirmPopupScreen(
			this,
			confirmed -> {
				if (confirmed) handler.restoreTriggerKeybinds(savedTriggerKeybinds);
				mc.setScreen(confirmed ? parent : this);
			},
			Translation.QUIT_CONFIRM_TITLE,
			Translation.QUIT_CONFIRM_WARNING,
			Translation.QUIT_CONFIRM_LABEL,
			OreButton.Style.DESTRUCTIVE,
			Translation.CANCEL_LABEL
		));
	}
	private boolean isActiveValue() {
		if (ClientLockManager.hasPendingLocks()) return false;
		if (!root.isActiveValue(config) || keybindCategory != null && !keybindCategory.isActiveValue(config)) return false;
		return savedTriggerKeybinds.equals(handler.getAllTriggerKeybinds());
	}
	@Override
	protected void init() {
		capturingEntry = null;
		ClientLockManager.clearPendingLocks();
		savedTriggerKeybinds = new LinkedHashMap<>(handler.getAllTriggerKeybinds());
		tabs.clear();
		root.resetToActive(config);
		for (var category : root.categories()) {
			var tab = new ConfigCategoryTab<>(this, category, config);
			tabs.add(tab);
		}
		var keybindCat = buildKeybindCategory();
		if (keybindCat != null) {
			keybindCat.resetToActive(config);
			keybindCategory = keybindCat;
			var keybindTab = new ConfigCategoryTab<>(this, keybindCategory, config);
			tabs.add(keybindTab);
		} else keybindCategory = null;
		initTabLayout();
		var buttonWidth = ConfigEntry.SIZE * 8;
		cancelButton = new OreButton(buttonWidth, getCancelLabel(), getCancelStyle(), b -> onClose());
		saveButton = new OreButton(buttonWidth, Translation.SAVE_CHANGES_LABEL, OreButton.Style.PRIMARY, b -> saveAndQuit());
		var footerLyt = layout.addToFooter(LinearLayout.horizontal().spacing(8));
		footerLyt.addChild(cancelButton);
		footerLyt.addChild(saveButton);
		layout.visitWidgets(w -> {
			w.setTabOrderGroup(1);
			addRenderableWidget(w);
		});
		repositionElements();
	}
	private @Nullable CategoryConfigNode<C> buildKeybindCategory() {
		var allMappings = ClientUtil.mc.options.keyMappings;
		var modMappings = new ArrayList<KeyMapping>();
		for (var allMapping : allMappings) {
			if (!allMapping.getCategory().equals("key.categories." + root.modId())) continue;
			modMappings.add(allMapping);
		}
		if (modMappings.isEmpty()) return null;
		var builder = CategoryConfigNode.<C>builder().title(Translation.KEYBINDS_LABEL);
		modMappings.forEach(mapping -> builder.<Key>value(value -> value.valueType(Key.class)
			.name(mapping.getName())
			.path("keybinds." + mapping.getName())
			.title(Component.translatable(mapping.getName()))
			.defaultValue(mapping.getDefaultKey())
			.valueReader(config -> ClientUtil.boundKey(mapping))
			.valueWriter((config, valueKey) -> mapping.setKey(valueKey))
			.restartType(null)));
		return builder.build();
	}
	private void initTabLayout() {
		tabNavigationBar = new OreTabNavigationBar(tabManager, tabs, width);
		initTabs(tabNavigationBar);
		addRenderableWidget(tabNavigationBar);
		tabNavigationBar.selectTab(lastTabIndex(), false);
	}
	private Component getCancelLabel() {
		return isActiveValue() ? Translation.BACK_LABEL : Translation.QUIT_UNSAVED_LABEL;
	}
	/** 与 {@link #getCancelLabel()} 同步：「取消」无损失用 Secondary，「放弃更改」会丢弃未保存的修改、用 Destructive。 */
	private OreButton.Style getCancelStyle() {
		return isActiveValue() ? OreButton.Style.SECONDARY : OreButton.Style.DESTRUCTIVE;
	}
	public void saveAndQuit() {
		if (!isEditable()) return;
		var restartType = root.restartType(config);
		root.writeEditingToConfig(config);
		if (keybindCategory != null) keybindCategory.writeEditingToConfig(config);
		var mc = ClientUtil.mc;
		mc.options.save();
		handler.save(config);
		ClientLockManager.flushPendingLocks();
		savedTriggerKeybinds = new LinkedHashMap<>(handler.getAllTriggerKeybinds());
		mc.setScreen(restartScreen(restartType));
	}
	@Override
	protected void repositionElements() {
		refresh();
		repositionTabLayout();
	}
	private void initTabs(@NotNull OreTabNavigationBar bar) {
		var i = 0;
		for (var button : bar.children()) tabs.get(i++).setTabButton(button);
	}
	private int lastTabIndex() {
		if (lastSelectedCategory == null) return 0;
		for (var i = 0; i < tabs.size(); i++)
			if (tabs.get(i).node == lastSelectedCategory) return i;
		return 0;
	}
	/**
	 * 客户端即服务端（房主）时可改：只看有没有内置服务端，<b>与存档是否开 LAN 无关</b> ——
	 * 房主就是那台内置服务端的主人；入服的玩家（LAN / 远程）没有内置服务端，天然只读。
	 */
	public boolean isEditable() {
		return handler.getType() != ConfigType.SERVER || ClientUtil.mc.hasSingleplayerServer();
	}
	/** 按重启级别给出后续界面：重载世界给「返回标题界面」，重启游戏给「退出游戏」。 */
	private Screen restartScreen(@Nullable RestartType restartType) {
		if (restartType == null) return parent;
		var mc = ClientUtil.mc;
		if (restartType == RestartType.WORLD) {
			if (mc.level == null) return parent;   // 不在世界里，没有可重载的世界
			return new OreConfirmPopupScreen(
				this,
				confirmed -> {
					if (confirmed) mc.disconnect();
					else mc.setScreen(parent);
				},
				Translation.RESTART_REQUIRED_TITLE,
				Translation.RESTART_REQUIRED_WORLD,
				mc.isLocalServer() ? Translation.RETURN_TO_MENU : CommonComponents.GUI_DISCONNECT,
				OreButton.Style.PRIMARY,
				Translation.IGNORE_RESTART_LABEL
			);
		}
		return new OreConfirmPopupScreen(
			this,
			confirmed -> {
				if (confirmed) mc.stop();
				else mc.setScreen(parent);
			},
			Translation.RESTART_REQUIRED_TITLE,
			Translation.RESTART_REQUIRED_LABEL,
			Translation.QUIT_GAME,
			OreButton.Style.DESTRUCTIVE,
			Translation.IGNORE_RESTART_LABEL
		);
	}
	public void refresh() {
		tabs.forEach(ConfigCategoryTab::refresh);
		var hasEntryError = tabs.stream().anyMatch(ConfigCategoryTab::hasEntryError);
		var blocked = saveBlockedReason(hasEntryError);
		saveButton.active = blocked == null;
		saveButton.setTooltip(Tooltip.create(blocked != null ? blocked : Translation.SAVE_CHANGES_TOOLTIP));
		cancelButton.setMessage(getCancelLabel());
		cancelButton.setStyle(getCancelStyle());
	}
	private void repositionTabLayout() {
		if (tabNavigationBar == null) return;
		tabNavigationBar.setWidth(width);
		tabNavigationBar.arrangeElements();
		var i = tabNavigationBar.getRectangle().bottom();
		var screenRectangle = new ScreenRectangle(0, i, width, height - layout.getFooterHeight() - i);
		tabManager.setTabArea(screenRectangle);
		layout.setHeaderHeight(i);
		layout.arrangeElements();
	}
	/** 保存键不可保存的原因；可保存时返回 {@code null}。 */
	private @Nullable Component saveBlockedReason(boolean hasEntryError) {
		if (!isEditable()) return Translation.tooltip(Translation.SAVE_UNAVAILABLE, Translation.SAVE_UNAVAILABLE_READ_ONLY);
		if (validate() != null || hasEntryError)
			return Translation.tooltip(Translation.SAVE_UNAVAILABLE, Translation.SAVE_UNAVAILABLE_INVALID);
		if (isActiveValue()) return Translation.tooltip(Translation.SAVE_UNAVAILABLE, Translation.SAVE_UNAVAILABLE_NO_CHANGES);
		return null;
	}
	private @Nullable Component validate() {
		var rootError = root.validate(config);
		if (rootError != null) return rootError;
		if (keybindCategory != null) return keybindCategory.validate(config);
		return null;
	}
	/** 由全局快捷键触发调用：将已由 entry 写入配置的值持久化到磁盘。 */
	public void saveByKeybind() {
		if (!isEditable()) return;
		handler.save(config);
	}
	public ConfigHandler<C, V> getHandler() {
		return handler;
	}
	/** 界面正在编辑的配置文件；热重载会跳过它们，免得冲掉未保存的编辑。 */
	public Set<FileKey> editedFiles() {
		return Set.of(new FileKey(root.modId(), root.fileName()));
	}
	/** 本界面所显示的配置是否支持服务端锁定（SERVER 型由服务端权威下发，不需要锁定）。 */
	public boolean isLockable() {
		return handler.getType() != ConfigType.SERVER;
	}
	public void onEntryCaptureChanged(CaptureHandler entry, boolean capturing) {
		capturingEntry = capturing ? entry : null;
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (capturingEntry != null && capturingEntry.handleCaptureMouse(button)) return true;
		var result = super.mouseClicked(mouseX, mouseY, button);
		cacheTabIndex();
		return result;
	}
	public int getHeaderHeight() {
		return layout.getHeaderHeight();
	}
	public int getFooterHeight() {
		return layout.getFooterHeight();
	}
}
