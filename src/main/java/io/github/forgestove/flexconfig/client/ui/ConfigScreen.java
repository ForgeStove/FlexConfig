package io.github.forgestove.flexconfig.client.ui;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.flexconfig.*;
import io.github.forgestove.flexconfig.api.*;
import io.github.forgestove.flexconfig.api.MenuOptions.Toggle;
import io.github.forgestove.flexconfig.client.*;
import io.github.forgestove.flexconfig.client.ui.core.*;
import io.github.forgestove.flexconfig.client.ui.core.UiFocus.Target;
import io.github.forgestove.flexconfig.client.ui.model.*;
import io.github.forgestove.flexconfig.client.ui.row.*;
import io.github.forgestove.flexconfig.client.ui.screen.*;
import io.github.forgestove.flexconfig.client.ui.widget.*;
import io.github.forgestove.flexconfig.client.ui.widget.UiButton.Style;
import io.github.forgestove.flexconfig.client.ui.widget.UiGlyphButton.Glyph;
import io.github.forgestove.flexconfig.client.ui.widget.UiPopupMenu.Item;
import io.github.forgestove.flexconfig.tree.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.*;
import org.lwjgl.glfw.GLFW;

import java.util.*;
public class ConfigScreen<C, V> extends Screen {
	public static CategoryConfigNode<?> lastSelectedCategory;
	public final RootConfigNode<C, V> root;
	private final C config;
	private final ConfigHandler<C, V> handler;
	private final Screen parent;
	private final UiContext ui = new UiContext();
	private final UiTween backdropAnim = new UiTween(0);
	private final UiTween windowAnim = new UiTween(0);
	private final UiTween riseAnim = new UiTween(UiMotion.RISE);
	private final UiTween closeAnim = new UiTween(1);
	private final UiAnim ringX = new UiAnim(0, UiMotion.INDICATOR_LEAD);
	private final UiAnim ringY = new UiAnim(0, UiMotion.INDICATOR_LEAD);
	private final UiAnim ringW = new UiAnim(0, UiMotion.INDICATOR_LEAD);
	private final UiAnim ringH = new UiAnim(0, UiMotion.INDICATOR_LEAD);
	private final ToastLayer toast = new ToastLayer();
	private final UiAnim searchWidth = new UiAnim(90, UiMotion.SEARCH_WIDTH);
	private final UiTween pageOut = new UiTween(1);
	private final UiTween pageIn = new UiTween(1);
	private ScreenLayout layout;
	private ConfigUiModel model;
	private SidebarView sidebar;
	private ShowcaseView showcase;
	private Toggle menuToggle;
	private UiToggle globalToggle;
	private FooterView footer;
	private UiScrollbar scrollbar;
	private UiButton doneButton, undoButton;
	private UiGlyphButton closeButton;
	private @Nullable UiControl pressedControl;
	private @Nullable ConfigRow pressedRow;
	private boolean sidebarPressed;
	private boolean ringInit;
	private int zoom = 1, logicalWidth, logicalHeight;
	private boolean opened;
	private boolean closing;
	private boolean dialogOpen;
	private Component dialogTitle = Component.empty(), dialogBody = Component.empty();
	private Runnable dialogAction = () -> {};
	private UiButton dialogConfirm, dialogCancel;
	private @Nullable Runnable closeAction;
	private @Nullable UiDropdown openDropdown;
	private @Nullable UiDropdown fadingDropdown;
	private @Nullable UiPopup openMenu;
	private UiTextField searchField;
	private UiChip changedChip, scopeChip;
	private UiButton pageMenuButton;
	private RowContext rowContext;
	private List<ConfigRow> outgoingRows = List.of();
	private double outgoingScroll;
	private boolean changedOnly, scopeCurrent;
	private String searchQuery = "";
	private List<ConfigRow> searchResults = List.of();
	private double searchScroll;
	private int lastWidth, lastHeight;
	private @Nullable CaptureTarget capture;
	private CategoryConfigNode<C> keybindCategory;
	private Map<String, String> savedTriggerKeybinds = Map.of();
	/** 首次打开才重置编辑值；从子界面返回时保留未保存的修改。 */
	private boolean initialized;
	public ConfigScreen(ConfigHandler<C, V> handler) {
		super(handler.getConfigTree().getTitle());
		root = handler.getConfigTree();
		config = handler.getConfig();
		this.handler = handler;
		parent = ClientUtil.mc.screen;
	}
	public Object config() {
		return config;
	}
	private void revealRows(List<ConfigRow> rows) {
		for (var i = 0; i < rows.size(); i++) {
			var stagger = Math.min(i, UiMotion.ROW_STAGGER_MAX) * UiMotion.ROW_STAGGER;
			rows.get(i).reveal(UiMotion.CONTENT_ROWS_DELAY + stagger);
		}
	}
	private List<CategoryConfigNode<?>> categories() {
		var list = new ArrayList<CategoryConfigNode<?>>(root.categories());
		if (keybindCategory != null) list.add(keybindCategory);
		return list;
	}
	private void selectRememberedTab() {
		if (lastSelectedCategory == null) return;
		var tabs = model.tabs();
		for (var i = 0; i < tabs.size(); i++)
			if (tabs.get(i).node == lastSelectedCategory) {
				model.setActive(i);
				return;
			}
	}
	private void updateScopeLabel() {
		if (scopeChip == null) return;
		var tab = model.activeTab();
		scopeChip.setLabel(scopeCurrent && tab != null
			? tab.title
			: Component.translatableWithFallback("flexconfig.search.scope_all", "All"));
	}
	private void updateSearch(String query) {
		searchQuery = query.trim();
		SearchIndex.clearHighlights(model);
		var tab = model.activeTab();
		searchResults = searchQuery.isEmpty()
			? List.of()
			: SearchIndex.search(rowContext, model, searchQuery, changedOnly, scopeCurrent ? tab : null);
		searchScroll = 0;
		if (scrollbar != null) scrollbar.setScroll(0);
		pageIn.start(0, 1, UiMotion.TAB_ROWS_IN);
		revealRows(searchResults);
	}
	private void cycleMenuSize() {
		UiPrefs.cycleSize();
		recomputeZoom();
		layout = new ScreenLayout(logicalWidth, logicalHeight);
		sidebar.setBounds(layout.tabs);
		footer.setBounds(layout.footer);
		scrollbar.setBounds(layout.scrollbar);
		ui.focus.clear();
	}
	private void openPageMenu() {
		var items = new ArrayList<Item>();
		items.add(new Item(
			Component.translatableWithFallback("flexconfig.menu.size", "Menu size: %s", UiPrefs.label().getString()),
			true,
			false,
			this::cycleMenuSize
		));
		items.add(new Item(
			Component.translatableWithFallback(
				"flexconfig.menu.high_contrast",
				"High contrast: %s",
				Component.translatableWithFallback(
						UiPrefs.highContrast() ? "flexconfig.menu.on" : "flexconfig.menu.off",
						UiPrefs.highContrast() ? "On" : "Off"
					)
					.getString()
			), true, UiPrefs.highContrast(), UiPrefs::toggleHighContrast
		));
		var tabs = model.tabs();
		var active = model.activeIndex();
		for (var i = 0; i < tabs.size(); i++) {
			var index = i;
			items.add(new Item(tabs.get(i).title, true, i == active, () -> selectTab(index)));
		}
		var menu = new UiPopupMenu(items, index -> items.get(index).action().run());
		menu.place(layout.panel, pageMenuButton.bounds().x(), pageMenuButton.bounds().bottom() + 2);
		openPopup(menu);
	}
	private void openRowMenu(ValueRow row, double mouseX, double mouseY) {
		var items = new ArrayList<Item>();
		items.add(new Item(
			Component.translatableWithFallback("flexconfig.row.reset", "Reset"),
			row.canReset(),
			false,
			row::resetToDefault
		));
		items.add(new Item(
			Component.translatableWithFallback("flexconfig.row.copy", "Copy value"),
			true,
			false,
			() -> ClientUtil.mc.keyboardHandler.setClipboard(row.valueText())
		));
		var menu = new UiPopupMenu(items, index -> items.get(index).action().run());
		menu.place(layout.panel, (int) mouseX, (int) mouseY);
		openPopup(menu);
	}
	private void selectTab(int index) {
		closeDropdown();
		var currentTab = model.activeTab();
		if (currentTab != null && searchQuery.isEmpty()) {
			outgoingRows = currentTab.rows;
			outgoingScroll = scrollbar.scroll();
			pageOut.start(1, 0, UiMotion.TAB_ROWS_IN);
			pageIn.start(0, 1, UiMotion.TAB_ROWS_IN);
		}
		if (searchField != null) searchField.setValue("");
		searchQuery = "";
		searchResults = List.of();
		searchScroll = 0;
		var current = model.activeTab();
		if (current != null && searchQuery.isEmpty()) current.scroll = scrollbar.scroll();
		model.setActive(index);
		var next = model.activeTab();
		if (next != null) {
			lastSelectedCategory = next.node;
			next.clampScroll(layout.content.height(), layout.content.width());
			scrollbar.setScroll(next.scroll);
			revealRows(next.rows);
		}
		updateScopeLabel();
		ui.focus.clear();
	}
	private void recomputeZoom() {
		zoom = UiPrefs.zoom(width, height);
		logicalWidth = Math.max(1, width / zoom);
		logicalHeight = Math.max(1, height / zoom);
	}
	@Override
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		var seconds = ClientUtil.mc.getTimer().getRealtimeDeltaTicks() / 20F;
		ui.onRefresh(this::refresh);
		var virtualX = mouseX / (double) zoom;
		var virtualY = mouseY / (double) zoom;
		ui.beginFrame(graphics, seconds, virtualX, virtualY);
		ui.viewport(logicalWidth, logicalHeight);
		backdropAnim.update(seconds);
		windowAnim.update(seconds);
		riseAnim.update(seconds);
		closeAnim.update(seconds);
		if (closing && closeAnim.done()) {
			var action = closeAction;
			closing = false;
			closeAction = null;
			if (action != null) action.run();
			return;
		}
		renderBackground(graphics, mouseX, mouseY, delta);
		if (layout == null || lastWidth != width || lastHeight != height) repositionElements();
		var painter = ui.painter;
		var closeFactor = closeAnim.value();
		painter.rect(0, 0, width, height, UiTheme.mulAlpha(UiTheme.BACKDROP_TOP, backdropAnim.value() * closeFactor));
		painter.pushScale(zoom);
		var rise = riseAnim.value() + (closing ? (1F - closeFactor) * UiMotion.CLOSE_DROP : 0);
		painter.pushAlpha(windowAnim.value() * closeFactor);
		painter.pushTranslate(0, rise);
		painter.rect(layout.panel.x() + 4, layout.panel.y() + 4, layout.panel.width(), layout.panel.height(), UiTheme.SHADOW);
		painter.rect(layout.panel.x() + 2, layout.panel.y() + 2, layout.panel.width(), layout.panel.height(), UiTheme.SHADOW);
		painter.border(
			layout.panel.x() - 1,
			layout.panel.y() - 1,
			layout.panel.width() + 2,
			layout.panel.height() + 2,
			UiTheme.WINDOW_EDGE
		);
		painter.rect(layout.header.x(), layout.header.y(), layout.header.width(), layout.header.height() - 3, UiTheme.TITLEBAR);
		painter.rect(layout.header.x(), layout.header.bottom() - 3, layout.header.width(), 1, UiTheme.TITLEBAR_EDGE);
		painter.rect(layout.header.x(), layout.header.bottom() - 1, layout.header.width(), 1, UiTheme.WINDOW_EDGE);
		painter.rect(layout.sidebar, UiTheme.SIDEBAR);
		painter.rect(
			layout.sidebar.right(),
			layout.sidebar.y(),
			layout.panel.right() - layout.sidebar.right(),
			layout.sidebar.height(),
			UiTheme.PANEL
		);
		painter.rect(layout.sidebar.right(), layout.sidebar.y(), 1, layout.sidebar.height(), UiTheme.WINDOW_EDGE);
		renderHeader(ui);
		showcase.render(ui, layout.showcase);
		if (globalToggle != null) {
			var label = menuToggle.label().getString();
			ui.painter.rect(layout.showcase.x() + 1, layout.showcase.bottom() - 21, layout.showcase.width() - 2, 20, 0xCC1E1E1F);
			ui.painter.text(
				ui.painter.trim(label, layout.showcase.width() - 52),
				layout.showcase.x() + 6,
				layout.showcase.bottom() - 20,
				UiTheme.TEXT_2
			);
			globalToggle.setBounds(new UiBounds(layout.showcase.right() - 6 - 30, layout.showcase.bottom() - 6 - 14, 30, 14));
			globalToggle.render(ui);
		}
		sidebar.render(ui);
		var tab = model.activeTab();
		var searching = !searchQuery.isEmpty();
		var rows = searching ? searchResults : tab == null ? List.<ConfigRow>of() : tab.rows;
		var height = 0;
		for (var row : rows) height += row.heightFor(layout.content.width());
		scrollbar.metrics(height, layout.content.height());
		scrollbar.update(seconds, mouseX, mouseY);
		if (searching) searchScroll = scrollbar.scroll();
		else if (tab != null) tab.scroll = scrollbar.scroll();
		pageOut.update(seconds);
		pageIn.update(seconds);
		if (!pageOut.done() && !outgoingRows.isEmpty()) renderOutgoing(ui, outgoingRows, outgoingScroll);
		ui.painter.pushAlpha(pageIn.value());
		ui.painter.pushTranslate((1F - pageIn.value()) * UiMotion.PAGE_SLIDE, 0);
		renderRows(ui, rows, searching ? searchScroll : tab == null ? 0 : tab.scroll);
		ui.painter.popTranslate();
		ui.painter.popAlpha();
		footer.render(ui);
		scrollbar.render(ui);
		renderFocusRing(ui);
		var popup = activePopup();
		if (popup != null) popup.renderPopup(ui);
		if (fadingDropdown != null) {
			fadingDropdown.renderPopup(ui);
			if (!fadingDropdown.closing()) fadingDropdown = null;
		}
		painter.popTranslate();
		painter.popAlpha();
		if (dialogOpen) renderDialog(ui);
		toast.update(seconds);
		toast.render(ui, logicalWidth, logicalHeight);
		if (!closing) ui.renderTooltip();
		painter.popScale();
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (closing) return true;
		if (dialogOpen) {
			if (keyCode == GLFW.GLFW_KEY_ESCAPE) cancelDialog();
			else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) confirmDialog();
			return true;
		}
		if (capture != null && capture.captureKey(keyCode)) return true;
		var popup = activePopup();
		if (popup != null && popup.popupKeyPressed(ui, keyCode, scanCode, modifiers)) return true;
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			var focused = ui.focus.current();
			if (focused instanceof UiTextField field) {
				if (field != searchField) {
					field.keyPressed(ui, keyCode, scanCode, modifiers);   // 编辑中先退出编辑
					return true;
				}
				if (!field.value().isEmpty()) {
					field.setValue("");
					updateSearch("");
					return true;
				}
				onClose();
				return true;
			}
			onClose();
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_TAB) {
			ui.setFocusVisible(true);
			ui.focus.move(hasShiftDown() ? -1 : 1);
			return true;
		}
		if (searchField != null && searchField.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		if (changedChip.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		if (scopeChip.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		if (pageMenuButton.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		if (globalToggle != null && globalToggle.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		if (undoButton.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		switch (keyCode) {
			case GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_HOME, GLFW.GLFW_KEY_END -> {
				if (navigateRow(keyCode)) return true;
			}
			default -> {}
		}
		if (keyCode == GLFW.GLFW_KEY_PAGE_UP || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
			var delta = keyCode == GLFW.GLFW_KEY_PAGE_UP ? -layout.content.height() : layout.content.height();
			scrollbar.setScroll(scrollbar.scroll() + delta);
			return true;
		}
		if (closeButton.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		for (var row : activeRows()) if (row.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		for (var control : footer.controls()) if (control.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
			onClose();
			return true;
		}
		return false;
	}
	@Override
	public void onClose() {
		if (closing) return;
		closeDropdown();
		capture = null;
		var mc = ClientUtil.mc;
		if (isActiveValue()) {
			beginClose(() -> mc.setScreen(parent));
			return;
		}
		showDialog(() -> {
			handler.restoreTriggerKeybinds(savedTriggerKeybinds);
			beginClose(() -> mc.setScreen(parent));
		});
	}
	@Override
	protected void init() {
		capture = null;
		ui.setAccent(handler.getAccent());
		if (!initialized) {
			initialized = true;
			ClientLockManager.clearPendingLocks();
			savedTriggerKeybinds = new LinkedHashMap<>(handler.getAllTriggerKeybinds());
			root.resetToActive(config);
		}
		if (!opened) {
			opened = true;
			backdropAnim.start(0, 1, UiMotion.BACKDROP_IN);
			windowAnim.start(0, 1, UiMotion.ENTER);
			riseAnim.start(UiMotion.RISE, 0, UiMotion.ENTER);
		} else {
			closing = false;
			closeAction = null;
			backdropAnim.snap(1);
			closeAnim.snap(1);
			windowAnim.snap(1);
			riseAnim.snap(0);
		}
		recomputeZoom();
		layout = new ScreenLayout(logicalWidth, logicalHeight);
		lastWidth = width;
		lastHeight = height;
		buildKeybindCategory();
		rowContext = new RowContext(this);
		model = new ConfigUiModel(rowContext);
		model.build(categories());
		selectRememberedTab();
		doneButton = new UiButton(Component.translatableWithFallback("flexconfig.done", "Done"), Style.PRIMARY, this::done);
		closeButton = new UiGlyphButton(Glyph.CLOSE, this::onClose);
		undoButton = new UiButton(Component.empty(), Style.SECONDARY, this::undoAll);
		undoButton.setVisible(false);
		dialogConfirm = new UiButton(Translation.QUIT_CONFIRM_LABEL, Style.PRIMARY, this::confirmDialog);
		dialogCancel = new UiButton(Translation.CANCEL_LABEL, Style.SECONDARY, this::cancelDialog);
		sidebar = new SidebarView(model, this::selectTab);
		var menuOptions = handler.getMenuOptions();
		showcase = new ShowcaseView(menuOptions.icon());
		menuToggle = menuOptions.toggle();
		globalToggle = menuToggle == null ? null : new UiToggle(
			menuToggle.state(), value -> {
			menuToggle.setter().accept(value);
			var node = root.getValueNode(menuToggle.fieldPath());
			if (node != null) node.resetToActive(config);
			handler.save(config);
			refresh();
		}
		);
		if (globalToggle != null) globalToggle.tooltip(menuToggle.tooltip());
		sidebar.setBounds(layout.tabs);
		footer = new FooterView(doneButton, this::statusText);
		footer.setBounds(layout.footer);
		scrollbar = new UiScrollbar();
		scrollbar.setBounds(layout.scrollbar);
		searchField = new UiTextField("");
		searchField.setLeadingSearch(true);
		searchField.setPlaceholder(Component.translatableWithFallback("flexconfig.search.placeholder", "Search settings"));
		searchField.setResponder(this::updateSearch);
		changedChip = new UiChip(
			Component.translatableWithFallback("flexconfig.search.changed", "Changed"), () -> {
			changedOnly = !changedOnly;
			changedChip.setSelected(changedOnly);
			updateSearch(searchField.value());
		}
		);
		scopeChip = new UiChip(
			Component.translatableWithFallback("flexconfig.search.scope_all", "All"), () -> {
			scopeCurrent = !scopeCurrent;
			updateScopeLabel();
			updateSearch(searchField.value());
		}
		);
		pageMenuButton = new UiButton(Component.literal("..."), Style.SECONDARY, this::openPageMenu);
		pageMenuButton.tooltip(Component.translatableWithFallback("flexconfig.search.menu", "Pages"));
		searchQuery = "";
		searchResults = List.of();
		searchScroll = 0;
		refresh();
		var activeTab = model.activeTab();
		revealRows(activeTab == null ? List.of() : activeTab.rows);
	}
	@Override
	protected void repositionElements() {
		if (model == null) return;
		recomputeZoom();
		layout = new ScreenLayout(logicalWidth, logicalHeight);
		lastWidth = width;
		lastHeight = height;
		sidebar.setBounds(layout.tabs);
		footer.setBounds(layout.footer);
		scrollbar.setBounds(layout.scrollbar);
		var tab = model.activeTab();
		if (tab != null) {
			tab.clampScroll(layout.content.height(), layout.content.width());
			scrollbar.setScroll(tab.scroll);
		}
	}
	private void renderDialog(UiContext ui) {
		var painter = ui.painter;
		var panel = new UiBounds((logicalWidth - 300) / 2, (logicalHeight - 140) / 2, 300, 140);
		painter.pushZ(300);
		painter.rect(0, 0, logicalWidth, logicalHeight, 0x99000000);
		painter.rect(panel.x() + 3, panel.y() + 3, panel.width(), panel.height(), UiTheme.SHADOW);
		painter.border(panel.x() - 1, panel.y() - 1, panel.width() + 2, panel.height() + 2, UiTheme.WINDOW_EDGE);
		painter.rect(panel.x(), panel.y(), panel.width(), UiTheme.HEADER_HEIGHT - 3, UiTheme.TITLEBAR);
		painter.rect(panel.x(), panel.y() + UiTheme.HEADER_HEIGHT - 3, panel.width(), 1, UiTheme.TITLEBAR_EDGE);
		painter.rect(panel.x(), panel.y() + UiTheme.HEADER_HEIGHT - 1, panel.width(), 1, UiTheme.WINDOW_EDGE);
		painter.rect(panel.x(), panel.y() + UiTheme.HEADER_HEIGHT, panel.width(), panel.height() - UiTheme.HEADER_HEIGHT, UiTheme.PANEL);
		painter.textEmphasis(dialogTitle, panel.x() + 8, panel.y() + 7, UiTheme.TITLE_INK);
		var lines = ClientUtil.mc.font.split(dialogBody, panel.width() - 16);
		var ty = panel.y() + UiTheme.HEADER_HEIGHT + 8;
		for (var line : lines) {
			painter.text(line, panel.x() + 8, ty, UiTheme.TEXT);
			ty += ClientUtil.mc.font.lineHeight;
		}
		dialogCancel.setBounds(new UiBounds(panel.right() - 8 - 70, panel.bottom() - 8 - 18, 70, 18));
		dialogConfirm.setBounds(new UiBounds(dialogCancel.bounds().x() - 4 - 70, dialogCancel.bounds().y(), 70, 18));
		dialogCancel.render(ui);
		dialogConfirm.render(ui);
		painter.popZ();
	}
	private void renderHeader(UiContext ui) {
		var painter = ui.painter;
		var title = ClientUtil.modName(root.modId()).getString().toUpperCase(Locale.ROOT);
		var titleY = layout.header.y() + (layout.header.height() - 3 - 10 + 1) / 2;
		painter.textEmphasis(
			title,
			layout.header.x() + (layout.header.width() - painter.textWidthEmphasis(title)) / 2,
			titleY,
			UiTheme.TITLE_INK
		);
		var headerText = Component.literal(root.fileName());
		if (root.restartType(config) != null)
			headerText = headerText.copy().append("  ·  ").append(Translation.RESTART_REQUIRED_TITLE.copy().withStyle(ChatFormatting.RED));
		painter.text(headerText, layout.pageHeader.x(), layout.pageHeader.y() + 4, UiTheme.TEXT_3);
		if (!searchQuery.isEmpty()) {
			var count = Component.translatableWithFallback("flexconfig.search.results", "%s results", searchResults.size());
			painter.text(count, layout.pageHeader.x() + painter.textWidth(headerText) + 8, layout.pageHeader.y() + 4, UiTheme.TEXT_3);
		}
		closeButton.setBounds(new UiBounds(layout.header.right() - 19, layout.header.y() + (layout.header.height() - 3 - 16) / 2, 16, 16));
		closeButton.render(ui);
		var undoWidth = 110;
		undoButton.setBounds(new UiBounds(closeButton.bounds().x() - 4 - undoWidth, layout.header.y() + 4, undoWidth, 14));
		undoButton.render(ui);
		var y = layout.pageHeader.y() + 3;
		var right = layout.pageHeader.right();
		pageMenuButton.setBounds(new UiBounds(right - 24, y, 24, UiTheme.CONTROL_HEIGHT));
		pageMenuButton.render(ui);
		right -= 28;
		var changedW = Math.min(90, changedChip.preferredWidth());
		changedChip.setBounds(new UiBounds(right - changedW, y, changedW, UiTheme.CONTROL_HEIGHT));
		changedChip.render(ui);
		right -= changedW + 4;
		var scopeW = Math.min(90, scopeChip.preferredWidth());
		scopeChip.setBounds(new UiBounds(right - scopeW, y, scopeW, UiTheme.CONTROL_HEIGHT));
		scopeChip.render(ui);
		right -= scopeW + 4;
		searchWidth.target(searchField.isFocused(ui) || !searchField.value().isEmpty() ? 140 : 90);
		searchWidth.update(ui.delta);
		searchField.setBounds(new UiBounds(
			right - (int) Math.round(searchWidth.get()),
			y,
			(int) Math.round(searchWidth.get()),
			UiTheme.CONTROL_HEIGHT
		));
		searchField.render(ui);
	}
	private void renderOutgoing(UiContext ui, List<ConfigRow> rows, double scroll) {
		var y = layout.content.y() - (int) Math.round(scroll);
		var content = layout.content;
		ui.painter.pushAlpha(pageOut.value());
		ui.painter.pushTranslate(-(1F - pageOut.value()) * UiMotion.PAGE_SLIDE, 0);
		ui.painter.scissorOn(content);
		for (var row : rows) {
			var height = row.heightFor(content.width());
			var bounds = new UiBounds(content.x(), y, content.width(), height);
			row.setBounds(bounds);
			if (bounds.intersects(content)) row.render(ui);
			y += height;
		}
		ui.painter.scissorOff();
		ui.painter.popTranslate();
		ui.painter.popAlpha();
	}
	private void renderRows(UiContext ui, List<ConfigRow> rows, double scroll) {
		var y = layout.content.y() - (int) Math.round(scroll);
		var focusables = new ArrayList<Target>();
		var content = layout.content;
		for (var row : rows) {
			var height = row.heightFor(content.width());
			var bounds = new UiBounds(content.x(), y, content.width(), height);
			row.setBounds(bounds);
			row.updateReveal(ui.delta);
			if (bounds.intersects(content)) {
				row.prepare(ui);
				row.layout();
			}
			y += height;
		}
		for (var row : rows)
			if (row.bounds().intersects(content)) for (var control : row.controls()) if (control.focusable()) focusables.add(control);
		if (searchField != null) focusables.add(searchField);
		focusables.add(changedChip);
		focusables.add(scopeChip);
		focusables.add(pageMenuButton);
		if (globalToggle != null) focusables.add(globalToggle);
		if (undoButton.visible()) focusables.add(undoButton);
		focusables.add(closeButton);
		for (var control : footer.controls()) if (control.focusable()) focusables.add(control);
		if (openDropdown != null) focusables.add(openDropdown.filterField());
		ui.focus.setOrder(focusables);
		ui.painter.scissorOn(content);
		for (var row : rows)
			if (row.bounds().intersects(content)) {
				var reveal = row.revealValue();
				if (reveal < 1) {
					ui.painter.pushAlpha(reveal);
					ui.painter.pushTranslate(0, (1F - reveal) * UiMotion.ROW_RISE);
					row.render(ui);
					ui.painter.popTranslate();
					ui.painter.popAlpha();
				} else row.render(ui);
			}
		ui.painter.scissorOff();
		var total = 0;
		for (var row : rows) total += row.heightFor(content.width());
		if (scroll > 0) ui.painter.gradient(new UiBounds(content.x(), content.y(), content.width(), 6), UiTheme.SHADOW, 0x00000000);
		if (scroll < Math.max(0, total - content.height()))
			ui.painter.gradient(new UiBounds(content.x(), content.bottom() - 6, content.width(), 6), 0x00000000, UiTheme.SHADOW);
	}
	private void renderFocusRing(UiContext ui) {
		var current = ui.focus.current();
		if (current == null || !ui.focusVisible() || current instanceof UiTextField) {
			ringInit = false;
			return;
		}
		var bounds = current.focusBounds().inset(-1);
		if (!ringInit) {
			ringX.jumpTo(bounds.x());
			ringY.jumpTo(bounds.y());
			ringW.jumpTo(bounds.width());
			ringH.jumpTo(bounds.height());
			ringInit = true;
		} else {
			ringX.target(bounds.x());
			ringY.target(bounds.y());
			ringW.target(bounds.width());
			ringH.target(bounds.height());
			ringX.update(ui.delta);
			ringY.update(ui.delta);
			ringW.update(ui.delta);
			ringH.update(ui.delta);
		}
		ui.painter.border(
			(int) Math.round(ringX.get()),
			(int) Math.round(ringY.get()),
			(int) Math.round(ringW.get()),
			Math.max(1, (int) Math.round(ringH.get())),
			ui.accent().light()
		);
	}
	private Component statusText() {
		if (!isEditable()) return Translation.SAVE_UNAVAILABLE_READ_ONLY;
		var count = model == null ? 0 : model.changedCount();
		if (model != null && model.hasError()) return Translation.SAVE_UNAVAILABLE_INVALID;
		return count == 0 ? null : Component.translatableWithFallback("flexconfig.status.changed", "%s changed", count);
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (closing) return true;
		mouseX /= zoom;
		mouseY /= zoom;
		ui.setFocusVisible(false);
		ui.pointer(mouseX, mouseY);
		if (dialogOpen) {
			if (dialogCancel.mouseClicked(ui, mouseX, mouseY, button)) {
				pressedControl = dialogCancel;
				return true;
			}
			if (dialogConfirm.mouseClicked(ui, mouseX, mouseY, button)) {
				pressedControl = dialogConfirm;
				return true;
			}
			return true;
		}
		ui.clearTooltip();
		if (capture != null) {
			capture.captureMouse(button);
			return true;
		}
		if (button == 1) {
			closeDropdown();
			if (layout.content.contains(mouseX, mouseY)) {
				var row = rowAt(activeRows(), activeScroll(), mouseY);
				if (row instanceof ValueRow valueRow) {
					openRowMenu(valueRow, mouseX, mouseY);
					return true;
				}
			}
			return false;
		}
		if (button != 0) return false;
		var popup = activePopup();
		if (popup != null) {
			if (popup.isOverPopup(mouseX, mouseY)) return popup.popupMouseClicked(ui, mouseX, mouseY, button);
			var onCell = openDropdown != null && openDropdown.bounds().contains(mouseX, mouseY);
			closeDropdown();
			if (onCell) return true;
		}
		if (changedChip.mouseClicked(ui, mouseX, mouseY, button)) {
			pressedControl = changedChip;
			return true;
		}
		if (scopeChip.mouseClicked(ui, mouseX, mouseY, button)) {
			pressedControl = scopeChip;
			return true;
		}
		if (pageMenuButton.mouseClicked(ui, mouseX, mouseY, button)) {
			pressedControl = pageMenuButton;
			return true;
		}
		if (globalToggle != null && globalToggle.mouseClicked(ui, mouseX, mouseY, button)) {
			pressedControl = globalToggle;
			return true;
		}
		if (undoButton.mouseClicked(ui, mouseX, mouseY, button)) {
			pressedControl = undoButton;
			return true;
		}
		if (closeButton.mouseClicked(ui, mouseX, mouseY, button)) {
			pressedControl = closeButton;
			return true;
		}
		if (searchField != null && searchField.mouseClicked(ui, mouseX, mouseY, button)) return true;
		if (sidebar.mouseClicked(ui, mouseX, mouseY, button)) {
			sidebarPressed = true;
			return true;
		}
		if (footer.mouseClicked(ui, mouseX, mouseY, button)) {
			pressedControl = downControl(footer.controls());
			return true;
		}
		if (scrollbar.mouseClicked(ui, mouseX, mouseY, button)) return true;
		if (layout.content.contains(mouseX, mouseY)) {
			var row = rowAt(activeRows(), activeScroll(), mouseY);
			if (row != null && row.mouseClicked(ui, mouseX, mouseY, button)) {
				var control = downControl(row.controls());
				if (control != null) pressedControl = control;
				else if (ui.dragOwner() == null) pressedRow = row;
				return true;
			}
		}
		ui.focus.clear();
		return false;
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button != 0) return false;
		mouseX /= zoom;
		mouseY /= zoom;
		ui.pointer(mouseX, mouseY);
		if (pressedControl != null) {
			var control = pressedControl;
			pressedControl = null;
			control.mouseReleased(ui, mouseX, mouseY, button);
			return true;
		}
		if (pressedRow != null) {
			var row = pressedRow;
			pressedRow = null;
			row.mouseReleased(ui, mouseX, mouseY, button);
			return true;
		}
		if (sidebarPressed) {
			sidebarPressed = false;
			sidebar.mouseReleased(ui, mouseX, mouseY, button);
			return true;
		}
		var handled = false;
		if (ui.dragOwner() instanceof UiControl control) handled = control.mouseReleased(ui, mouseX, mouseY, button);
		var popup = activePopup();
		if (!handled && popup != null) handled = popup.popupMouseReleased(ui, mouseX, mouseY, button);
		if (!handled) handled = scrollbar.mouseReleased(ui, mouseX, mouseY, button);
		if (handled) ui.setDragOwner(null);
		return handled;
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		mouseX /= zoom;
		mouseY /= zoom;
		ui.pointer(mouseX, mouseY);
		if (ui.dragOwner() instanceof UiControl control) return control.mouseDragged(ui, mouseX, mouseY, button, dragX, dragY);
		return scrollbar.shown() && scrollbar.mouseDragged(ui, mouseX, mouseY, button, dragX, dragY);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (dialogOpen) return true;
		mouseX /= zoom;
		mouseY /= zoom;
		var popup = activePopup();
		if (popup != null && popup.isOverPopup(mouseX, mouseY)) return popup.popupMouseScrolled(scrollY);
		if (layout.tabs.contains(mouseX, mouseY)) {
			sidebar.wheel(scrollY);
			return true;
		}
		scrollbar.wheel(scrollY, UiTheme.ROW_HEIGHT + UiTheme.ROW_GAP);
		return true;
	}
	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		if (closing || dialogOpen) return true;
		if (capture != null) return true;
		var popup = activePopup();
		if (popup != null && popup.popupCharTyped(ui, codePoint)) return true;
		if (searchField != null && searchField.charTyped(ui, codePoint)) return true;
		for (var row : activeRows()) if (row.charTyped(ui, codePoint)) return true;
		return false;
	}
	private @Nullable UiControl downControl(List<? extends UiControl> controls) {
		for (var control : controls) if (control.down()) return control;
		return null;
	}
	private boolean navigateRow(int keyCode) {
		var rows = activeRows();
		var controls = new ArrayList<UiControl>();
		for (var row : rows) for (var control : row.controls()) if (control.focusable()) controls.add(control);
		if (controls.isEmpty()) return false;
		var current = ui.focus.current();
		var index = current instanceof UiControl control ? controls.indexOf(control) : -1;
		int next;
		switch (keyCode) {
			case GLFW.GLFW_KEY_UP -> next = index <= 0 ? controls.size() - 1 : index - 1;
			case GLFW.GLFW_KEY_DOWN -> next = index < 0 || index >= controls.size() - 1 ? 0 : index + 1;
			case GLFW.GLFW_KEY_HOME -> next = 0;
			case GLFW.GLFW_KEY_END -> next = controls.size() - 1;
			default -> {
				return false;
			}
		}
		ui.setFocusVisible(true);
		ui.focus.set(controls.get(next));
		ensureRowVisible(rows, controls.get(next));
		return true;
	}
	private void ensureRowVisible(List<ConfigRow> rows, UiControl target) {
		var y = layout.content.y() - (int) Math.round(activeScroll());
		for (var row : rows) {
			var height = row.heightFor(layout.content.width());
			for (var control : row.controls())
				if (control == target) {
					var bottom = y + height;
					if (y < layout.content.y()) scrollbar.setScroll(scrollbar.scroll() - (layout.content.y() - y));
					else if (bottom > layout.content.bottom()) scrollbar.setScroll(scrollbar.scroll() + (bottom - layout.content.bottom()));
					return;
				}
			y += height;
		}
	}
	private List<ConfigRow> activeRows() {
		if (!searchQuery.isEmpty()) return searchResults;
		var tab = model.activeTab();
		return tab == null ? List.of() : tab.rows;
	}
	private double activeScroll() {
		if (!searchQuery.isEmpty()) return searchScroll;
		var tab = model.activeTab();
		return tab == null ? 0 : tab.scroll;
	}
	private @Nullable ConfigRow rowAt(List<ConfigRow> rows, double scroll, double mouseY) {
		var y = layout.content.y() - (int) Math.round(scroll);
		for (var row : rows) {
			var height = row.heightFor(layout.content.width());
			var bounds = new UiBounds(layout.content.x(), y, layout.content.width(), height);
			if (bounds.contains(layout.content.x() + 1, mouseY)) {
				row.setBounds(bounds);
				row.prepare(ui);
				row.layout();
				return row;
			}
			y += height;
		}
		return null;
	}
	private void showDialog(Runnable action) {
		closeDropdown();
		capture = null;
		dialogTitle = Translation.QUIT_CONFIRM_TITLE;
		dialogBody = Translation.QUIT_CONFIRM_WARNING;
		dialogAction = action;
		dialogOpen = true;
		ui.focus.clear();
	}
	private void confirmDialog() {
		if (!dialogOpen) return;
		dialogOpen = false;
		UiSound.click();
		dialogAction.run();
	}
	private void cancelDialog() {
		if (!dialogOpen) return;
		dialogOpen = false;
		UiSound.click();
	}
	private void beginClose(Runnable action) {
		closing = true;
		closeAction = action;
		closeAnim.start(1, 0, UiMotion.EXIT);
	}
	public void saveAndQuit() {
		if (!isEditable()) return;
		var restartType = root.restartType(config);
		root.writeEditingToConfig(config);
		if (keybindCategory != null) keybindCategory.writeEditingToConfig(config);
		var mc = ClientUtil.mc;
		mc.options.save();
		if (!handler.save(config)) return;
		ClientLockManager.flushPendingLocks();
		savedTriggerKeybinds = new LinkedHashMap<>(handler.getAllTriggerKeybinds());
		var screen = restartScreen(restartType);
		beginClose(() -> mc.setScreen(screen));
	}
	private Screen restartScreen(@Nullable RestartType restartType) {
		if (restartType == null) return parent;
		var mc = ClientUtil.mc;
		if (restartType == RestartType.WORLD) {
			if (mc.level == null) return parent;
			return new ConfirmScreen(
				confirmed -> {
					if (confirmed) mc.disconnect();
					else mc.setScreen(parent);
				},
				Translation.RESTART_REQUIRED_TITLE,
				Translation.RESTART_REQUIRED_WORLD,
				mc.isLocalServer() ? Translation.RETURN_TO_MENU : CommonComponents.GUI_DISCONNECT,
				Translation.IGNORE_RESTART_LABEL
			);
		}
		return new ConfirmScreen(
			confirmed -> {
				if (confirmed) mc.stop();
				else mc.setScreen(parent);
			},
			Translation.RESTART_REQUIRED_TITLE,
			Translation.RESTART_REQUIRED_LABEL,
			Translation.QUIT_GAME,
			Translation.IGNORE_RESTART_LABEL
		);
	}
	public void refresh() {
		if (model == null) return;
		model.refreshAll();
		if (doneButton == null) return;
		doneButton.tooltip(doneBlockedReason());
		var changed = model.changedCount();
		undoButton.setVisible(changed > 0 && isEditable());
		undoButton.setLabel(Component.translatableWithFallback("flexconfig.undo_changes", "Undo changes (%s)", changed));
	}
	public void undoAll() {
		root.resetToActive(config);
		if (keybindCategory != null) keybindCategory.resetToActive(config);
		handler.restoreTriggerKeybinds(savedTriggerKeybinds);
		ClientLockManager.clearPendingLocks();
		refresh();
		for (var tab : model.tabs()) for (var row : tab.rows) if (row instanceof ValueRow valueRow) valueRow.wash();
		toast.info(Component.translatableWithFallback("flexconfig.toast.undone", "Changes undone"));
	}
	public void rebuildActiveTab() {
		if (model == null) return;
		closeDropdown();
		model.rebuildActive();
		var tab = model.activeTab();
		if (tab != null) {
			tab.clampScroll(layout.content.height(), layout.content.width());
			scrollbar.setScroll(tab.scroll);
			revealRows(tab.rows);
		}
		refresh();
	}
	private void done() {
		if (!isEditable() || isActiveValue()) {
			onClose();
			return;
		}
		if (validate() != null || model.hasError()) return;   // 无效值不保存也不关闭
		saveAndQuit();
	}
	private @Nullable Component doneBlockedReason() {
		if (!isEditable()) return Translation.tooltip(Translation.SAVE_UNAVAILABLE, Translation.SAVE_UNAVAILABLE_READ_ONLY);
		if (validate() != null || model.hasError())
			return Translation.tooltip(Translation.SAVE_UNAVAILABLE, Translation.SAVE_UNAVAILABLE_INVALID);
		return null;
	}
	private @Nullable Component validate() {
		var rootError = root.validate(config);
		if (rootError != null) return rootError;
		if (keybindCategory != null) return keybindCategory.validate(config);
		return null;
	}
	public boolean isActiveValue() {
		if (ClientLockManager.hasPendingLocks()) return false;
		if (!root.isActiveValue(config) || keybindCategory != null && !keybindCategory.isActiveValue(config)) return false;
		return savedTriggerKeybinds.equals(handler.getAllTriggerKeybinds());
	}
	public void saveByKeybind() {
		if (!isEditable()) return;
		handler.save(config);
	}
	public boolean isEditable() {
		return handler.getType() != ConfigType.SERVER || ClientUtil.mc.hasSingleplayerServer();
	}
	public boolean isLockable() {
		return ConfigRegistry.isLockable(new FileKey(root.modId(), root.fileName()));
	}
	public ConfigHandler<C, V> getHandler() {
		return handler;
	}
	public Accent accent() {
		return handler.getAccent();
	}
	public Set<FileKey> editedFiles() {
		return Set.of(new FileKey(root.modId(), root.fileName()));
	}
	public void beginCapture(CaptureTarget target) {
		closeDropdown();
		capture = target;
		ui.focus.clear();
	}
	private @Nullable UiPopup activePopup() {
		return openMenu != null ? openMenu : openDropdown;
	}
	public void openPopup(UiPopup popup) {
		closeDropdown();
		openMenu = popup;
	}
	public void openDropdown(UiDropdown dropdown) {
		if (openDropdown == dropdown) {
			closeDropdown();
			return;
		}
		closeDropdown();
		openMenu = null;
		dropdown.setViewport(layout.content);
		dropdown.setOpen(true);
		openDropdown = dropdown;
		ui.focus.set(dropdown.filterField());
	}
	public void closeDropdown() {
		if (openDropdown != null) {
			openDropdown.beginClose();
			fadingDropdown = openDropdown;
		}
		if (openMenu != null) openMenu.close();
		openDropdown = null;
		openMenu = null;
	}
	public void endCapture(CaptureTarget target) {
		if (capture == target) capture = null;
	}
	private void buildKeybindCategory() {
		var allMappings = ClientUtil.mc.options.keyMappings;
		var modMappings = new ArrayList<KeyMapping>();
		for (var mapping : allMappings) if (mapping.getCategory().equals("key.categories." + root.modId())) modMappings.add(mapping);
		if (modMappings.isEmpty()) {
			keybindCategory = null;
			return;
		}
		var builder = CategoryConfigNode.<C>builder().title(Translation.KEYBINDS_LABEL);
		modMappings.forEach(mapping -> builder.<Key>value(value -> value.valueType(Key.class)
			.name(mapping.getName())
			.path("keybinds." + mapping.getName())
			.title(Component.translatable(mapping.getName()))
			.defaultValue(mapping.getDefaultKey())
			.valueReader(config -> ClientUtil.boundKey(mapping))
			.valueWriter((config, valueKey) -> mapping.setKey(valueKey))
			.restartType(null)));
		var built = builder.build();
		built.resetToActive(config);
		keybindCategory = built;
	}
}
