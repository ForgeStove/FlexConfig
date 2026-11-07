package io.github.forgestove.flexconfig.foundation.ui;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.flexconfig.api.*;
import io.github.forgestove.flexconfig.api.menu.Accent;
import io.github.forgestove.flexconfig.api.registration.ConfigHandler;
import io.github.forgestove.flexconfig.foundation.*;
import io.github.forgestove.flexconfig.foundation.client.*;
import io.github.forgestove.flexconfig.foundation.tree.*;
import io.github.forgestove.flexconfig.foundation.ui.ConfigInput.*;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.core.UiFocus.Target;
import io.github.forgestove.flexconfig.foundation.ui.model.*;
import io.github.forgestove.flexconfig.foundation.ui.row.*;
import io.github.forgestove.flexconfig.foundation.ui.screen.*;
import io.github.forgestove.flexconfig.foundation.ui.widget.*;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiButton.Style;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiGlyphButton.Glyph;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiPopupMenu.Item;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.*;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.*;
import org.lwjgl.glfw.GLFW;

import java.util.*;
public final class ConfigShell<C, V> {
	public static CategoryConfigNode lastSelectedCategory;
	public final RootConfigNode root;
	private final ConfigHost host;
	private final C config;
	private final ConfigHandler<C, V> handler;
	private final UiContext ui = new UiContext();
	private final UiTween backdropAnim = new UiTween(0);
	private final UiTween windowAnim = new UiTween(0);
	private final UiTween riseAnim = new UiTween(UiMotion.RISE);
	private final UiTween closeAnim = new UiTween(1);
	private final UiAnim ringX = new UiAnim(0, UiMotion.INDICATOR_LEAD);
	private final UiAnim ringY = new UiAnim(0, UiMotion.INDICATOR_LEAD);
	private final UiAnim ringW = new UiAnim(0, UiMotion.INDICATOR_LEAD);
	private final UiAnim ringH = new UiAnim(0, UiMotion.INDICATOR_LEAD);
	private final UiTween ringAlpha = new UiTween(0);
	private final ToastLayer toast = new ToastLayer();
	private final UiAnim searchWidth = new UiAnim(90, UiMotion.SEARCH_WIDTH);
	private final UiTween pageOut = new UiTween(1);
	private final UiTween pageIn = new UiTween(1);
	private ScreenLayout layout;
	private ConfigUiModel model;
	private SidebarView sidebar;
	private ShowcaseView showcase;
	private FooterView footer;
	private UiScrollPane contentPane;
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
	private @Nullable UiPopup fadingMenu;
	private UiTextField searchField;
	private UiChip changedChip;
	private UiButton pageMenuButton;
	private RowContext rowContext;
	private List<ConfigRow> outgoingRows = List.of();
	private double outgoingScroll;
	private boolean changedOnly;
	private String searchQuery = "";
	private List<ConfigRow> searchResults = List.of();
	private int lastWidth, lastHeight;
	private @Nullable CaptureTarget capture;
	/** 正在播放退出动画的行及其后续操作；整棵子树作为一组一起收起。 */
	private final List<PendingRemoval> pendingRemovals = new ArrayList<>();
	private boolean suppressRebuildFx;
	private CategoryConfigNode keybindCategory;
	private Map<String, String> savedTriggerKeybinds = Map.of();
	/** 首次打开才重置编辑值；从子界面返回时保留未保存的修改。 */
	private boolean initialized;
	private boolean closed;
	public ConfigShell(ConfigHost host, ConfigHandler<C, V> handler) {
		this.host = host;
		root = handler.getConfigTree();
		config = handler.getConfig();
		this.handler = handler;
	}
	public boolean closed() {
		return closed;
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
	private List<CategoryConfigNode> categories() {
		var list = new ArrayList<>(root.categories());
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
	private void updateSearch(String query) {
		if (!pendingRemovals.isEmpty()) rebuildActiveTab();
		searchQuery = query.trim();
		SearchIndex.clearHighlights(model);
		searchResults = searchQuery.isEmpty() && !changedOnly ? List.of() : SearchIndex.search(rowContext, model, searchQuery,
			changedOnly);
		var tab = model.activeTab();
		contentPane.setScroll(searchQuery.isEmpty() && !changedOnly && tab != null ? tab.scroll : 0);
		pageIn.start(0, 1, UiMotion.TAB_ROWS_IN);
		revealRows(searchResults);
	}
	private void openPageMenu() {
		var items = new ArrayList<Item>();
		var tab = model.activeTab();
		items.add(new Item(Translation.PAGE_RESET, tab != null && tab.changedCount > 0, false, this::resetPage));
		var menu = new UiPopupMenu(items, index -> items.get(index).action().run());
		menu.place(layout.panel, pageMenuButton.bounds().x(), pageMenuButton.bounds().bottom() + 2);
		openPopup(menu);
	}
	private void resetPage() {
		var tab = model.activeTab();
		if (tab == null) return;
		var restores = new ArrayList<Runnable>();
		for (var row : tab.rows)
			if (row instanceof ValueRow valueRow && valueRow.canReset()) {
				var node = valueRow.node();
				var previous = FieldAccess.deepCopy(node.getEditingValue(config));
				valueRow.resetToDefault();
				restores.add(() -> node.setEditingValue(previous));
			}
		if (restores.isEmpty()) return;
		refresh();
		toast.info(
			Translation.pageResetToast(restores.size()), Translation.UNDO_TOOLTIP, () -> {
				for (var restore : restores) restore.run();
				refresh();
				toast.info(Translation.TOAST_UNDONE);
			}
		);
	}
	private void openRowMenu(ValueRow row, double mouseX, double mouseY) {
		var items = new ArrayList<Item>();
		items.add(new Item(row.resetLabel(), row.canReset(), false, row::resetToDefault));
		items.add(new Item(Translation.ROW_COPY, true, false, () -> host.copyToClipboard(row.valueText())));
		var menu = new UiPopupMenu(items, index -> items.get(index).action().run());
		menu.place(layout.panel, (int) mouseX, (int) mouseY);
		openPopup(menu);
	}
	private void selectTab(int index) {
		if (!pendingRemovals.isEmpty()) rebuildActiveTab();
		closeDropdown();
		var currentTab = model.activeTab();
		if (currentTab != null && searchQuery.isEmpty()) {
			outgoingRows = currentTab.rows;
			outgoingScroll = contentPane.scroll();
			pageOut.start(1, 0, UiMotion.TAB_ROWS_IN);
			pageIn.start(0, 1, UiMotion.TAB_ROWS_IN);
		}
		if (searchField != null) searchField.setValue("");
		searchQuery = "";
		searchResults = List.of();
		var current = model.activeTab();
		if (current != null && searchQuery.isEmpty()) current.scroll = contentPane.scroll();
		model.setActive(index);
		var next = model.activeTab();
		if (next != null) {
			lastSelectedCategory = next.node;
			next.clampScroll(layout.content.height(), layout.content.width());
			contentPane.setScroll(next.scroll);
			revealRows(next.rows);
		}
		ui.focus.clear();
	}
	private void recomputeZoom() {
		zoom = UiPrefs.zoom(host.guiWidth(), host.guiHeight());
		logicalWidth = Math.max(1, host.guiWidth() / zoom);
		logicalHeight = Math.max(1, host.guiHeight() / zoom);
	}
	@SuppressWarnings("unused")
	public void frame(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
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
		if (!pendingRemovals.isEmpty() && pendingRemovals.stream().allMatch(PendingRemoval::finished)) {
			suppressRebuildFx = true;
			try {
				rebuildActiveTab();
			} finally {
				suppressRebuildFx = false;
			}
		}
		if (closing && closeAnim.done()) {
			var action = closeAction;
			closing = false;
			closeAction = null;
			closed = true;
			if (action != null) action.run();
			return;
		}
		if (layout == null || lastWidth != host.guiWidth() || lastHeight != host.guiHeight()) resize();
		var painter = ui.painter;
		var closeFactor = closeAnim.value();
		painter.rect(0, 0, host.guiWidth(), host.guiHeight(), UiTheme.mulAlpha(UiTheme.BACKDROP_TOP, backdropAnim.value() * closeFactor));
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
		sidebar.render(ui);
		var tab = model.activeTab();
		var searching = !searchQuery.isEmpty() || changedOnly;
		var rows = searching ? searchResults : tab == null ? List.<ConfigRow>of() : tab.rows;
		var height = 0;
		for (var row : rows) height += row.displayHeight(layout.content.width());
		contentPane.setContentHeight(height);
		pageOut.update(seconds);
		pageIn.update(seconds);
		if (!pageOut.done() && !outgoingRows.isEmpty()) renderOutgoing(ui, outgoingRows, outgoingScroll);
		ui.painter.pushAlpha(pageIn.value());
		ui.painter.pushTranslate((1F - pageIn.value()) * UiMotion.PAGE_SLIDE, 0);
		contentPane.render(ui);
		ui.painter.popTranslate();
		ui.painter.popAlpha();
		if (!searching && tab != null) tab.scroll = contentPane.scroll();
		footer.render(ui);
		renderFocusRing(ui);
		if (openMenu != null && openMenu.closing()) {
			fadingMenu = openMenu;
			openMenu = null;
		}
		var popup = activePopup();
		if (popup != null) popup.renderPopup(ui);
		if (fadingMenu != null) {
			fadingMenu.renderPopup(ui);
			if (!fadingMenu.closing()) fadingMenu = null;
		}
		if (fadingDropdown != null) {
			fadingDropdown.renderPopup(ui);
			if (!fadingDropdown.closing()) fadingDropdown = null;
		}
		painter.popTranslate();
		painter.popAlpha();
		if (dialogOpen) renderDialog(ui);
		toast.update(ui, seconds);
		toast.render(ui, logicalWidth, logicalHeight);
		if (!closing) ui.renderTooltip();
		painter.popScale();
	}
	private boolean key(int keyCode, int scanCode, int modifiers) {
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
				if (!field.value().isEmpty() || changedOnly) {
					field.setValue("");
					changedOnly = false;
					changedChip.setSelected(false);
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
			ui.focus.move(host.shiftDown() ? -1 : 1);
			return true;
		}
		if (searchField != null && searchField.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		if (changedChip.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		if (pageMenuButton.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		if (undoButton.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		switch (keyCode) {
			case GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_DOWN, GLFW.GLFW_KEY_HOME, GLFW.GLFW_KEY_END -> {
				if (navigateRow(keyCode)) return true;
			}
			default -> {}
		}
		if (keyCode == GLFW.GLFW_KEY_PAGE_UP || keyCode == GLFW.GLFW_KEY_PAGE_DOWN) {
			var delta = keyCode == GLFW.GLFW_KEY_PAGE_UP ? -layout.content.height() : layout.content.height();
			contentPane.scrollBy(delta);
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
	public void onClose() {
		if (closing) return;
		closeDropdown();
		capture = null;
		if (isActiveValue()) {
			beginClose(() -> host.setScreen(host.parentScreen()));
			return;
		}
		showDialog(() -> {
			handler.restoreTriggerKeybinds(savedTriggerKeybinds);
			beginClose(() -> host.setScreen(host.parentScreen()));
		});
	}
	public void onRemoved() {
		pressedControl = null;
		pressedRow = null;
		sidebarPressed = false;
		ui.setDragOwner(null);
		ui.clearTooltip();
	}
	public void init() {
		capture = null;
		pressedControl = null;
		pressedRow = null;
		sidebarPressed = false;
		openDropdown = null;
		fadingDropdown = null;
		openMenu = null;
		fadingMenu = null;
		flushPendingRemovals();
		suppressRebuildFx = false;
		outgoingRows = List.of();
		ui.setDragOwner(null);
		ui.focus.clear();
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
			closed = false;
			closeAction = null;
			backdropAnim.snap(1);
			closeAnim.snap(1);
			windowAnim.snap(1);
			riseAnim.snap(0);
		}
		recomputeZoom();
		layout = new ScreenLayout(logicalWidth, logicalHeight, handler.getMenuOptions().icon() != null);
		lastWidth = host.guiWidth();
		lastHeight = host.guiHeight();
		buildKeybindCategory();
		rowContext = new RowContext(this);
		model = new ConfigUiModel(rowContext);
		model.build(categories());
		selectRememberedTab();
		doneButton = new UiButton(Translation.DONE_LABEL.copy().withStyle(ChatFormatting.BOLD), Style.PRIMARY, this::done);
		closeButton = new UiGlyphButton(Glyph.CLOSE, this::onClose);
		undoButton = new UiButton(Component.empty(), Style.SECONDARY, this::undoAll);
		undoButton.setVisible(false);
		dialogConfirm = new UiButton(
			Translation.QUIT_CONFIRM_LABEL.copy().withStyle(ChatFormatting.BOLD),
			Style.PRIMARY,
			this::confirmDialog
		);
		dialogCancel = new UiButton(Translation.CANCEL_LABEL, Style.SECONDARY, this::cancelDialog);
		sidebar = new SidebarView(model, this::selectTab);
		showcase = new ShowcaseView(handler.getMenuOptions().icon());
		sidebar.setBounds(layout.tabs);
		footer = new FooterView(doneButton);
		footer.setBounds(layout.footer);
		contentPane = new UiScrollPane() {
			@Override
			protected void renderContent(UiContext ui) {
				renderRows(ui);
			}
		};
		contentPane.setBounds(contentPaneBounds());
		contentPane.setPitch(UiTheme.ROW_HEIGHT + UiTheme.ROW_GAP);
		searchField = new UiTextField("");
		searchField.setLeadingSearch(true);
		searchField.setPlaceholder(Translation.SEARCH_PLACEHOLDER);
		searchField.setResponder(this::updateSearch);
		changedChip = new UiChip(
			Component.empty(), () -> {
			changedOnly = !changedOnly;
			changedChip.setSelected(changedOnly);
			updateSearch(searchField.value());
		}
		);
		pageMenuButton = new UiButton(Component.literal("..."), Style.SECONDARY, this::openPageMenu);
		pageMenuButton.tooltip(Translation.SEARCH_MENU);
		searchQuery = "";
		searchResults = List.of();
		refresh();
		if (changedOnly) updateSearch("");
		var activeTab = model.activeTab();
		revealRows(activeTab == null ? List.of() : activeTab.rows);
	}
	public void resize() {
		if (model == null) return;
		recomputeZoom();
		layout = new ScreenLayout(logicalWidth, logicalHeight, handler.getMenuOptions().icon() != null);
		lastWidth = host.guiWidth();
		lastHeight = host.guiHeight();
		sidebar.setBounds(layout.tabs);
		footer.setBounds(layout.footer);
		contentPane.setBounds(contentPaneBounds());
		var tab = model.activeTab();
		if (tab != null) {
			tab.clampScroll(layout.content.height(), layout.content.width());
			contentPane.setScroll(tab.scroll);
		}
	}
	private UiBounds contentPaneBounds() {
		var content = layout.content;
		return new UiBounds(content.x(), content.y(), layout.scrollbar.right() - content.x(), content.height());
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
		if (!searchQuery.isEmpty() || changedOnly) {
			var count = Translation.searchResults(searchResults.size());
			painter.text(count, layout.pageHeader.x() + painter.textWidth(headerText) + 8, layout.pageHeader.y() + 4, UiTheme.TEXT_3);
		}
		closeButton.setBounds(new UiBounds(
			layout.header.right() - 19,
			layout.header.y() + Math.round((layout.header.height() - 3 - 16) / 2F),
			16,
			16
		));
		closeButton.render(ui);
		var undoWidth = 110;
		undoButton.setBounds(new UiBounds(closeButton.bounds().x() - 4 - undoWidth, layout.header.y() + 4, undoWidth, 14));
		undoButton.render(ui);
		var y = layout.pageHeader.y() + 3;
		var right = layout.pageHeader.right();
		var tab = model.activeTab();
		pageMenuButton.setVisible(searchQuery.isEmpty() && !changedOnly && tab != null && tab.changedCount > 0);
		if (pageMenuButton.visible()) {
			pageMenuButton.setBounds(new UiBounds(right - 24, y, 24, UiTheme.CONTROL_HEIGHT));
			pageMenuButton.render(ui);
			right -= 28;
		}
		var changedW = Math.min(90, changedChip.preferredWidth());
		changedChip.setBounds(new UiBounds(right - changedW, y, changedW, UiTheme.CONTROL_HEIGHT));
		changedChip.render(ui);
		right -= changedW + 4;
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
	private void renderRows(UiContext ui) {
		var rows = activeRows();
		var content = layout.content;
		var y = content.y() - (int) Math.round(contentPane.scroll());
		var focusables = new ArrayList<Target>();
		for (var row : rows) {
			var height = row.heightFor(content.width());
			var shown = row.displayHeight(content.width());
			var bounds = new UiBounds(content.x(), y, content.width(), height);
			row.setBounds(bounds);
			row.updateReveal(ui.delta);
			if (bounds.intersects(content)) {
				row.prepare(ui);
				row.layout();
			}
			y += shown;
		}
		for (var row : rows)
			if (row.bounds().intersects(content)) for (var control : row.controls()) if (control.focusable()) focusables.add(control);
		if (searchField != null) focusables.add(searchField);
		focusables.add(changedChip);
		if (pageMenuButton.focusable()) focusables.add(pageMenuButton);
		if (undoButton.visible()) focusables.add(undoButton);
		focusables.add(closeButton);
		for (var control : footer.controls()) if (control.focusable()) focusables.add(control);
		if (openDropdown != null) focusables.add(openDropdown.filterField());
		ui.focus.setOrder(focusables);
		for (var row : rows)
			if (row.bounds().intersects(content)) {
				var reveal = row.revealValue();
				var scale = row.heightScale();
				var shrink = scale < 0.999F;
				if (reveal < 1) ui.painter.pushAlpha(reveal);
				if (shrink) {
					ui.painter.pushAlpha(Math.max(0.01F, scale));
					ui.painter.scissorOn(new UiBounds(content.x(), row.bounds().y(), content.width(), row.displayHeight(content.width())));
				}
				row.render(ui);
				if (shrink) {
					ui.painter.scissorOff();
					ui.painter.popAlpha();
				}
				if (reveal < 1) ui.painter.popAlpha();
			}
	}
	private void renderFocusRing(UiContext ui) {
		var current = ui.focus.current();
		if (current == null || !ui.focusVisible()) {
			ringInit = false;
			ringAlpha.start(ringAlpha.value(), 0, UiMotion.TOOLTIP_FADE);
			ringAlpha.update(ui.delta);
			return;
		}
		if (ringAlpha.target() < 1) ringAlpha.start(ringAlpha.value(), 1, UiMotion.TOOLTIP_FADE);
		ringAlpha.update(ui.delta);
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
		if (ringAlpha.value() <= 0.01F) return;
		ui.painter.pushAlpha(ringAlpha.value());
		var x = (int) Math.round(ringX.get());
		var y = (int) Math.round(ringY.get());
		var width = (int) Math.round(ringW.get());
		var height = Math.max(1, (int) Math.round(ringH.get()));
		ui.painter.border(x - 1, y - 1, width + 2, height + 2, 0x80000000);
		ui.painter.border(x, y, width, height, ui.accent().light());
		ui.painter.popAlpha();
	}
	public boolean input(ConfigInput event) {
		return switch (event) {
			case MouseDown e -> mouseDown(e.x(), e.y(), e.button());
			case MouseUp e -> mouseUp(e.x(), e.y(), e.button());
			case MouseDrag e -> mouseDrag(e.x(), e.y(), e.button(), e.dragX(), e.dragY());
			case Scroll e -> scroll(e.x(), e.y(), e.scrollX(), e.scrollY());
			case ConfigInput.Key e -> key(e.keyCode(), e.scanCode(), e.modifiers());
			case Char e -> charType(e.codePoint(), e.modifiers());
		};
	}
	private boolean mouseDown(double mouseX, double mouseY, int button) {
		if (closing) return true;
		mouseX /= zoom;
		mouseY /= zoom;
		ui.setFocusVisible(false);
		ui.pointer(mouseX, mouseY);
		if (toast.mouseClicked(mouseX, mouseY, button)) return true;
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
		if (pageMenuButton.mouseClicked(ui, mouseX, mouseY, button)) {
			pressedControl = pageMenuButton;
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
		if (contentPane.mouseClicked(ui, mouseX, mouseY, button)) return true;
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
	private boolean mouseUp(double mouseX, double mouseY, int button) {
		if (button != 0) return false;
		mouseX /= zoom;
		mouseY /= zoom;
		ui.pointer(mouseX, mouseY);
		if (toast.mouseReleased(mouseX, mouseY, button)) return true;
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
		if (!handled) handled = contentPane.mouseReleased(ui, mouseX, mouseY, button);
		if (handled) ui.setDragOwner(null);
		return handled;
	}
	private boolean mouseDrag(double mouseX, double mouseY, int button, double dragX, double dragY) {
		mouseX /= zoom;
		mouseY /= zoom;
		ui.pointer(mouseX, mouseY);
		if (ui.dragOwner() instanceof UiControl control) return control.mouseDragged(ui, mouseX, mouseY, button, dragX, dragY);
		if (sidebar.mouseDragged(ui, mouseX, mouseY, button, dragX, dragY)) return true;
		return contentPane.mouseDragged(ui, mouseX, mouseY, button, dragX, dragY);
	}
	@SuppressWarnings("unused")
	private boolean scroll(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (dialogOpen) return true;
		mouseX /= zoom;
		mouseY /= zoom;
		var popup = activePopup();
		if (popup != null && popup.isOverPopup(mouseX, mouseY)) return popup.popupMouseScrolled(scrollY);
		if (layout.tabs.contains(mouseX, mouseY)) {
			sidebar.wheel(scrollY);
			return true;
		}
		return contentPane.mouseScrolled(ui, mouseX, mouseY, scrollY);
	}
	@SuppressWarnings("unused")
	private boolean charType(char codePoint, int modifiers) {
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
		var content = layout.content;
		for (var row : rows) {
			var height = row.displayHeight(content.width());
			for (var control : row.controls())
				if (control == target) {
					var bottom = y + height;
					if (y < content.y()) contentPane.scrollBy(y - content.y());
					else if (bottom > content.bottom()) contentPane.scrollBy(bottom - content.bottom());
					return;
				}
			y += height;
		}
	}
	private List<ConfigRow> activeRows() {
		if (!searchQuery.isEmpty() || changedOnly) return searchResults;
		var tab = model.activeTab();
		return tab == null ? List.of() : tab.rows;
	}
	private double activeScroll() {
		return contentPane.scroll();
	}
	private @Nullable ConfigRow rowAt(List<ConfigRow> rows, double scroll, double mouseY) {
		var y = layout.content.y() - (int) Math.round(scroll);
		var content = layout.content;
		for (var row : rows) {
			var height = row.heightFor(content.width());
			var shown = row.displayHeight(content.width());
			var bounds = new UiBounds(content.x(), y, content.width(), height);
			if (mouseY >= y && mouseY < y + shown) {
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
		beginClose(() -> host.setScreen(screen));
	}
	private Screen restartScreen(@Nullable RestartType restartType) {
		if (restartType == null) return host.parentScreen();
		var mc = ClientUtil.mc;
		if (restartType == RestartType.WORLD) {
			if (mc.level == null) return host.parentScreen();
			return new ConfirmScreen(
				confirmed -> {
					if (confirmed) mc.disconnect();
					else host.setScreen(host.parentScreen());
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
				else host.setScreen(host.parentScreen());
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
		undoButton.setLabel(Translation.undoChanges(changed));
		changedChip.setLabel(Translation.searchChanged(changed));
		changedChip.setSelected(changedOnly);
	}
	public void undoAll() {
		root.resetToActive(config);
		if (keybindCategory != null) keybindCategory.resetToActive(config);
		handler.restoreTriggerKeybinds(savedTriggerKeybinds);
		ClientLockManager.clearPendingLocks();
		refresh();
		for (var tab : model.tabs()) for (var row : tab.rows) if (row instanceof ValueRow valueRow) valueRow.wash();
		toast.info(Translation.TOAST_UNDONE);
	}
	public void exitRow(Object identity, Runnable action) {
		var key = String.valueOf(identity);
		var rows = new ArrayList<ConfigRow>();
		for (var row : activeRows()) {
			var id = row.identity();
			if (id == null) continue;
			var path = id.toString();
			// 标题行连同它的字段行、嵌套元素行作为一整棵子树一起收起
			if (path.equals(key) || path.startsWith(key + ".") || path.startsWith(key + "[")) rows.add(row);
		}
		if (rows.isEmpty()) {
			action.run();
			rebuildActiveTab();
			return;
		}
		if (rows.stream().allMatch(ConfigRow::exiting)) return;   // 重复点击，已经在收起
		for (var row : rows) row.onExit();
		pendingRemovals.add(new PendingRemoval(rows, action, null));
	}
	/** 收起一个集合的全部元素行，重建后让新的元素行重新展开；用于集合重置。 */
	public void exitElements(String collectionPath, Runnable action) {
		var prefix = collectionPath + "[";
		var rows = new ArrayList<ConfigRow>();
		for (var row : activeRows()) {
			var id = row.identity();
			if (id != null && id.toString().startsWith(prefix)) rows.add(row);
		}
		if (!rows.isEmpty() && rows.stream().allMatch(ConfigRow::exiting)) return;   // 重复点击，已经在收起
		for (var row : rows) row.onExit();
		pendingRemovals.add(new PendingRemoval(rows, action, collectionPath));
	}
	private void animateCollection(String collectionPath) {
		if (model == null) return;
		var tab = model.activeTab();
		if (tab == null) return;
		var prefix = collectionPath + "[";
		for (var row : tab.rows) {
			var id = row.identity();
			if (id == null) continue;
			var path = id.toString();
			if (path.startsWith(prefix)) row.expandFrom(0);
			else if (path.equals(collectionPath) && row instanceof ValueRow valueRow) valueRow.wash();
		}
	}
	/** 立即结束所有待处理的删除；返回重建后需要重新展开的集合路径。 */
	private List<String> flushPendingRemovals() {
		if (pendingRemovals.isEmpty()) return List.of();
		var pending = List.copyOf(pendingRemovals);
		pendingRemovals.clear();
		var expansions = new ArrayList<String>();
		for (var removal : pending) {
			removal.action().run();
			if (removal.expandPath() != null) expansions.add(removal.expandPath());
		}
		return expansions;
	}
	public void rebuildActiveTab() {
		if (model == null) return;
		closeDropdown();
		var hadPending = !pendingRemovals.isEmpty();
		var expansions = flushPendingRemovals();
		// 刚处理过删除/重置时，旧行的收缩比例属于已被移除的项，不能套到同路径的新行上
		var quiet = suppressRebuildFx || hadPending;
		var tab = model.activeTab();
		var oldRows = tab == null ? List.<ConfigRow>of() : List.copyOf(tab.rows);
		var oldIds = new HashSet<>();
		var expanding = new HashMap<Object, Float>();
		for (var row : oldRows) {
			var id = row.identity();
			if (id == null) continue;
			oldIds.add(id);
			if (row.heightScale() < 1) expanding.put(id, row.heightScale());
		}
		model.rebuildActive();
		if (tab != null) {
			var newRows = tab.rows;
			var newIds = new HashSet<>();
			for (var row : newRows) {
				var id = row.identity();
				if (id != null) newIds.add(id);
			}
			var removed = false;
			for (var id : oldIds)
				if (!newIds.contains(id)) {
					removed = true;
					break;
				}
			for (var row : newRows) {
				var id = row.identity();
				if (id == null) continue;
				if (!oldIds.contains(id)) {
					if (!quiet) row.onEnter();
				} else if (!quiet) {
					var scale = expanding.get(id);
					if (scale != null) row.expandFrom(scale);
				}
			}
			if (removed && !quiet) pageIn.start(0.5F, 1, UiMotion.TAB_ROWS_IN);
			tab.clampScroll(layout.content.height(), layout.content.width());
			contentPane.setScroll(tab.scroll);
		}
		for (var path : expansions) animateCollection(path);
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
		return ConfigStore.isLockable(new FileKey(root.modId(), root.fileName()));
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
		if (openMenu != null) {
			openMenu.close();
			fadingMenu = openMenu;
		}
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
		var builder = CategoryConfigNode.builder().title(Translation.KEYBINDS_LABEL);
		modMappings.forEach(mapping -> builder.value(value -> value.valueType(Key.class)
			.name(mapping.getName())
			.path("keybinds." + mapping.getName())
			.title(Component.translatable(mapping.getName()))
			.defaultValue(mapping.getDefaultKey())
			.valueReader(config -> ClientUtil.boundKey(mapping))
			.valueWriter((config, valueKey) -> mapping.setKey((Key) valueKey))
			.restartType(null)));
		var built = builder.build();
		built.resetToActive(config);
		keybindCategory = built;
	}
	private record PendingRemoval(List<ConfigRow> rows, Runnable action, @Nullable String expandPath) {
		boolean finished() {
			for (var row : rows) if (!row.exited()) return false;
			return true;
		}
	}
}
