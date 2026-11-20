package io.github.forgestove.flexconfig.foundation.ui;
import io.github.forgestove.flexconfig.api.*;
import io.github.forgestove.flexconfig.api.registration.ConfigHandler;
import io.github.forgestove.flexconfig.foundation.client.*;
import io.github.forgestove.flexconfig.foundation.tree.CategoryConfigNode;
import io.github.forgestove.flexconfig.foundation.ui.ConfigInput.*;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.core.UiFocus.Target;
import io.github.forgestove.flexconfig.foundation.ui.model.*;
import io.github.forgestove.flexconfig.foundation.ui.row.*;
import io.github.forgestove.flexconfig.foundation.ui.screen.*;
import io.github.forgestove.flexconfig.foundation.ui.screen.HeaderBar.Info;
import io.github.forgestove.flexconfig.foundation.ui.widget.*;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiButton.Style;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiPopupMenu.Item;
import net.minecraft.client.gui.screens.*;
import org.jetbrains.annotations.*;
import org.lwjgl.glfw.GLFW;

import java.util.*;
public final class ConfigShell implements UiView, RowContext {
	public static CategoryConfigNode lastSelectedCategory;
	private final ConfigHost host;
	private final EditingSession<?, ?> session;
	private final UiContext ui = new UiContext();
	private final UiTween backdropAnim = new UiTween(0);
	private final UiTween windowAnim = new UiTween(0);
	private final UiTween riseAnim = new UiTween(UiMotion.RISE);
	private final UiTween closeAnim = new UiTween(1);
	private final UiFocusRing focusRing = new UiFocusRing();
	private final ToastLayer toast = new ToastLayer();
	private final UiTween pageOut = new UiTween(1);
	private final UiTween pageIn = new UiTween(1);
	private final UiPointerRouter pointer = new UiPointerRouter();
	private final DialogView dialog = new DialogView();
	private final UiPopupHost popups = new UiPopupHost();
	private final SearchSession search;
	private final RemovalQueue removals = new RemovalQueue();
	/** 已被筛掉的旧结果行：先播完收起动画，再从列表里移除。 */
	private final List<LeavingRow> leaving = new ArrayList<>();
	private byte pageDirection = 1;
	private ScreenLayout layout;
	private ConfigUiModel model;
	private SidebarView sidebar;
	private ShowcaseView showcase;
	private FooterView footer;
	private RowListView contentPane;
	private UiButton doneButton;
	private HeaderBar header;
	private int zoom = 1, logicalWidth, logicalHeight;
	private boolean opened;
	private boolean closing;
	private @Nullable Runnable closeAction;
	private List<ConfigRow> outgoingRows = List.of();
	private double outgoingScroll;
	private int modelVersion;
	private int lastWidth, lastHeight;
	private @Nullable CaptureTarget capture;
	private boolean suppressRebuildFx;
	private boolean ready;
	private boolean closed;
	/** {@code anchor} 是旧列表里排在它后面且仍然存在的行，用来在合并视图里保持原位。 */
	private record LeavingRow(ConfigRow row, ConfigRow anchor) {}
	public ConfigShell(ConfigHost host, ConfigHandler<?, ?> handler) {
		this.host = host;
		session = new EditingSession<>(handler);
		search = new SearchSession(this, this::clearSearch);
	}
	@Override
	public boolean closed() {
		return closed;
	}
	private void revealRows(List<ConfigRow> rows) {
		for (var i = 0; i < rows.size(); i++) {
			var stagger = Math.min(i, UiMotion.ROW_STAGGER_MAX) * UiMotion.ROW_STAGGER;
			rows.get(i).reveal(UiMotion.CONTENT_ROWS_DELAY + stagger);
		}
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
		if (!removals.isEmpty()) rebuildActiveTab();
		dropLeavingRows();
		search.setQuery(query);
		search.rebuild(model, modelVersion);
		var tab = model.activeTab();
		contentPane.setScroll(!search.filtering() && tab != null ? tab.scroll : 0);
		pageIn.start(0, 1, UiMotion.TAB_ROWS_IN);
		revealRows(search.rows());
	}
	private void rebuildSearchResults() {
		var before = search.filtering() ? List.copyOf(search.rows()) : null;
		search.rebuild(model, modelVersion);
		if (before == null || !search.filtering()) {
			dropLeavingRows();
			return;
		}
		startLeavingRows(before, search.rows());
	}
	/** 结果里消失的值行先收起；合并视图会在动画结束前继续渲染它们。 */
	private void startLeavingRows(List<ConfigRow> before, List<ConfigRow> after) {
		var afterById = new HashMap<Object, ConfigRow>();
		var afterSet = Collections.newSetFromMap(new IdentityHashMap<ConfigRow, Boolean>());
		for (var row : after) {
			afterSet.add(row);
			var id = row.identity();
			if (id != null) afterById.put(id, row);
		}
		// 撤销等操作可能让正在退出的行重新回到结果里
		leaving.removeIf(item -> {
			var row = item.row();
			var id = row.identity();
			if (!afterSet.contains(row) && (id == null || !afterById.containsKey(id))) return false;
			row.cancelExit();
			return true;
		});
		for (var i = 0; i < before.size(); i++) {
			var row = before.get(i);
			if (!(row instanceof ValueRow) || row.exiting()) continue;
			var id = row.identity();
			if (id != null && afterById.containsKey(id) || afterSet.contains(row)) continue;
			ConfigRow anchor = null;
			for (var j = i + 1; j < before.size() && anchor == null; j++) {
				var next = before.get(j);
				var nextId = next.identity();
				if (nextId != null) anchor = afterById.get(nextId);
				if (anchor == null && afterSet.contains(next)) anchor = next;
			}
			// 结果清空时提示行是新实例，把离开的行放在它上方
			if (anchor == null && after.size() == 1 && after.getFirst() instanceof SearchEmptyRow) anchor = after.get(0);
			row.onExit();
			leaving.add(new LeavingRow(row, anchor));
		}
	}
	/** 待离开的行只是暂时不显示，清空时要把行本身恢复到正常状态。 */
	private void dropLeavingRows() {
		for (var item : leaving) item.row().cancelExit();
		leaving.clear();
	}
	private void clearSearch() {
		search.clear();
		header.setChangedSelected(false);
		header.searchField().setValue("");
		updateSearch("");
	}
	private void toggleChanged() {
		search.setChangedOnly(!search.changedOnly());
		header.setChangedSelected(search.changedOnly());
		updateSearch(header.searchField().value());
	}
	private void openPageMenu() {
		var items = new ArrayList<Item>();
		var tab = model.activeTab();
		items.add(new Item(Translation.PAGE_RESET, tab != null && !tab.isDefaultValue(), false, this::resetPage));
		var menu = new UiPopupMenu(items, index -> items.get(index).action().run());
		menu.place(layout.panel, header.pageMenuBounds().x(), header.pageMenuBounds().bottom() + 2);
		openPopup(menu);
	}
	private void resetPage() {
		var tab = model.activeTab();
		if (tab == null) return;
		var restores = new ArrayList<Runnable>();
		for (var row : tab.rows)
			if (row instanceof ValueRow valueRow && valueRow.canReset()) {
				var node = valueRow.node();
				var previous = node.copyForEditing(node.getEditingValue(session.config()));
				valueRow.resetToDefault();
				restores.add(() -> node.setEditingValue(previous));
			}
		if (restores.isEmpty()) return;
		refresh();
		toast.info(
			Translation.pageResetToast(restores.size()), Translation.UNDO_TOOLTIP, () -> {
				removals.flush();   // 先执行挂起的集合重置，否则恢复出的列表会被随后执行的重置覆盖
				for (var restore : restores) restore.run();
				rebuildActiveTab();
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
		if (!removals.isEmpty()) rebuildActiveTab();
		closeDropdown();
		var wasFiltering = search.filtering();
		if (index == model.activeIndex()) {
			ui.focus.clear();
			return;
		}
		var currentTab = model.activeTab();
		if (!wasFiltering) {
			pageDirection = (byte) Integer.signum(index - model.activeIndex());
			if (pageDirection == 0) pageDirection = 1;
			if (currentTab != null) {
				outgoingRows = currentTab.rows;
				outgoingScroll = contentPane.scroll();
				currentTab.scroll = contentPane.scroll();
				pageOut.start(1, 0, UiMotion.TAB_ROWS_IN);
				pageIn.start(0, 1, UiMotion.TAB_ROWS_IN);
			}
			header.searchField().setValue("");
			dropLeavingRows();
			search.clear();
			search.rebuild(model, modelVersion);
			header.setChangedSelected(false);
		}
		model.setActive(index);
		var next = model.activeTab();
		if (next == null) {
			ui.focus.clear();
			return;
		}
		lastSelectedCategory = next.node;
		// 筛选结果跨分类，只改当前分类，保留筛选条件和列表位置
		if (!wasFiltering) {
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
	@Override
	public void frame(@NotNull UiRenderTarget target, int mouseX, int mouseY, float delta) {
		if (!ready) return;
		var seconds = ClientUtil.mc.getTimer().getRealtimeDeltaTicks() / 20F;
		ui.onRefresh(this::refresh);
		var virtualX = mouseX / (double) zoom;
		var virtualY = mouseY / (double) zoom;
		ui.beginFrame(target, seconds, virtualX, virtualY);
		ui.viewport(logicalWidth, logicalHeight);
		backdropAnim.update(seconds);
		windowAnim.update(seconds);
		riseAnim.update(seconds);
		closeAnim.update(seconds);
		if (!removals.isEmpty() && removals.finished()) {
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
		painter.rect(layout.header.x(), layout.header.y(), layout.header.width(), layout.header.height() - 2, UiTheme.TITLEBAR);
		painter.rect(layout.header.x(), layout.header.bottom() - 2, layout.header.width(), 1, UiTheme.TITLEBAR_EDGE);
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
		header.render(ui, layout, headerInfo());
		showcase.render(ui, layout.showcase);
		sidebar.render(ui);
		if (search.needsRebuild(modelVersion) && ui.dragOwner() == null) rebuildSearchResults();
		var tab = model.activeTab();
		contentPane.updateContentHeight();
		pageOut.update(seconds);
		pageIn.update(seconds);
		if (!pageOut.done() && !outgoingRows.isEmpty()) renderOutgoing(ui, outgoingRows, outgoingScroll);
		ui.painter.pushAlpha(pageIn.value());
		ui.painter.pushTranslate((1F - pageIn.value()) * UiMotion.PAGE_SLIDE_IN * pageDirection, 0);
		contentPane.render(ui);
		for (var iterator = leaving.iterator(); iterator.hasNext(); ) {
			var row = iterator.next().row();
			if (!row.exited()) continue;
			row.cancelExit();
			iterator.remove();
		}
		ui.painter.popTranslate();
		ui.painter.popAlpha();
		var focusables = new ArrayList<Target>();
		contentPane.addFocusables(focusables);
		focusables.addAll(header.focusables());
		for (var control : footer.controls()) if (control.focusable()) focusables.add(control);
		var openDropdown = popups.openDropdown();
		if (openDropdown != null) focusables.add(openDropdown.filterField());
		ui.focus.setOrder(focusables);
		if (!search.filtering() && tab != null) tab.scroll = contentPane.scroll();
		footer.render(ui);
		focusRing.render(ui);
		popups.render(ui);
		painter.popTranslate();
		painter.popAlpha();
		if (dialog.open()) dialog.render(ui, logicalWidth, logicalHeight);
		toast.update(ui, seconds);
		toast.render(ui, logicalWidth, logicalHeight);
		if (!closing) ui.renderTooltip();
		painter.popScale();
	}
	private boolean key(int keyCode, int scanCode, int modifiers) {
		if (closing) return true;
		if (dialog.open()) return dialog.keyPressed(keyCode);
		if (capture != null && capture.captureKey(keyCode)) return true;
		var popup = activePopup();
		if (popup != null && popup.popupKeyPressed(ui, keyCode, scanCode, modifiers)) return true;
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			var focused = ui.focus.current();
			if (focused instanceof UiTextField field) {
				if (field != header.searchField()) {
					field.keyPressed(ui, keyCode, scanCode, modifiers);   // 编辑中先退出编辑
					return true;
				}
				if (!field.value().isEmpty() || search.changedOnly()) {
					clearSearch();
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
		if (header.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
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
		if (header.closeKeyPressed(ui, keyCode, scanCode, modifiers)) return true;
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
		if (isActiveValue()) {
			beginClose(() -> host.setScreen(host.parentScreen()));
			return;
		}
		showDialog(() -> {
			session.restoreTriggerKeybinds();
			beginClose(() -> host.setScreen(host.parentScreen()));
		});
	}
	@Override
	public void onRemoved() {
		pointer.clear();
		ui.setDragOwner(null);
		ui.clearTooltip();
	}
	@Override
	public void init() {
		capture = null;
		pointer.clear();
		popups.clear();
		removals.flush();
		dropLeavingRows();
		suppressRebuildFx = false;
		outgoingRows = List.of();
		ui.setDragOwner(null);
		ui.focus.clear();
		ui.setAccent(session.handler().getAccent());
		session.begin();
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
		layout = new ScreenLayout(logicalWidth, logicalHeight, session.handler().getMenuOptions().icon() != null);
		lastWidth = host.guiWidth();
		lastHeight = host.guiHeight();
		session.buildKeybindCategory();
		model = new ConfigUiModel(this);
		model.build(session.categories());
		modelVersion++;
		selectRememberedTab();
		doneButton = new UiButton(Translation.DONE_LABEL.copy().withStyle(UiStyle.BOLD), Style.PRIMARY, this::done);
		header = new HeaderBar(this::onClose, this::undoAll, this::openPageMenu, this::updateSearch, this::toggleChanged);
		sidebar = new SidebarView(model, this::selectTab);
		showcase = new ShowcaseView(Translation.texture(session.handler().getMenuOptions().icon()));
		sidebar.setBounds(layout.tabs);
		footer = new FooterView(doneButton);
		footer.setBounds(layout.footer);
		contentPane = new RowListView(layout.content, this::activeRows);
		contentPane.setBounds(contentPaneBounds());
		contentPane.setPitch(UiTheme.ROW_HEIGHT + UiTheme.ROW_GAP);
		search.clearQuery();
		search.rebuild(model, modelVersion);
		refresh();
		if (search.changedOnly()) updateSearch("");
		var activeTab = model.activeTab();
		revealRows(activeTab == null ? List.of() : activeTab.rows);
		ready = true;
	}
	@Override
	public void resize() {
		if (model == null) return;
		recomputeZoom();
		layout = new ScreenLayout(logicalWidth, logicalHeight, session.handler().getMenuOptions().icon() != null);
		lastWidth = host.guiWidth();
		lastHeight = host.guiHeight();
		sidebar.setBounds(layout.tabs);
		footer.setBounds(layout.footer);
		contentPane.setBounds(contentPaneBounds());
		contentPane.setContent(layout.content);
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
	private Info headerInfo() {
		var tab = model.activeTab();
		return new Info(
			session.root().modId(),
			session.root().fileName(),
			session.restartType() != null,
			search.filtering(),
			search.count(),
			!search.filtering() && tab != null && !tab.isDefaultValue()
		);
	}
	private void renderOutgoing(UiContext ui, List<ConfigRow> rows, double scroll) {
		var y = layout.content.y() - (int) Math.round(scroll);
		var content = layout.content;
		ui.painter.pushAlpha(pageOut.value());
		ui.painter.pushTranslate(-(1F - pageOut.value()) * UiMotion.PAGE_SLIDE_OUT * pageDirection, 0);
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
	@Override
	public boolean input(ConfigInput event) {
		// 初始化失败时控件不完整，只放行 Esc 退回上一屏
		if (!ready) {
			if (event instanceof Key key && key.keyCode() == GLFW.GLFW_KEY_ESCAPE) host.setScreen(host.parentScreen());
			return true;
		}
		return switch (event) {
			case MouseDown e -> mouseDown(e.x(), e.y(), e.button());
			case MouseUp e -> mouseUp(e.x(), e.y(), e.button());
			case MouseDrag e -> mouseDrag(e.x(), e.y(), e.button(), e.dragX(), e.dragY());
			case Scroll e -> scroll(e.x(), e.y(), e.scrollY());
			case Key e -> key(e.keyCode(), e.scanCode(), e.modifiers());
			case Char e -> charType(e.codePoint());
		};
	}
	private boolean mouseDown(double mouseX, double mouseY, int button) {
		if (closing) return true;
		mouseX /= zoom;
		mouseY /= zoom;
		ui.setFocusVisible(false);
		ui.pointer(mouseX, mouseY);
		if (toast.mouseClicked(mouseX, mouseY, button)) return true;
		if (dialog.open()) {
			dialog.mouseDown(ui, pointer, mouseX, mouseY, button);
			return true;
		}
		if (capture != null) {
			capture.captureMouse(button);
			return true;
		}
		if (button == 1) {
			closeDropdown();
			if (layout.content.contains(mouseX, mouseY)) {
				var row = contentPane.rowAt(ui, mouseY);
				if (row instanceof ValueRow valueRow && !row.exiting()) {
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
			var openDropdown = popups.openDropdown();
			var onCell = openDropdown != null && openDropdown.bounds().contains(mouseX, mouseY);
			closeDropdown();
			if (onCell) return true;
		}
		if (header.press(pointer, ui, mouseX, mouseY, button)) return true;
		if (pointer.press(ui, sidebar, mouseX, mouseY, button)) return true;
		for (var control : footer.controls()) if (pointer.press(ui, control, mouseX, mouseY, button)) return true;
		if (pointer.press(ui, contentPane, mouseX, mouseY, button)) return true;
		if (layout.content.contains(mouseX, mouseY)) {
			var row = contentPane.rowAt(ui, mouseY);
			if (pointer.press(ui, row, mouseX, mouseY, button)) return true;
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
		if (pointer.pressed() != null) return pointer.release(ui, mouseX, mouseY, button);
		var handled = false;
		var popup = activePopup();
		if (popup != null) handled = popup.popupMouseReleased(ui, mouseX, mouseY, button);
		if (!handled) handled = contentPane.mouseReleased(ui, mouseX, mouseY, button);
		// 左键释放意味着本次手势结束，无论控件是否消费都要清掉拖拽主体，避免残留吞掉后续点击
		ui.setDragOwner(null);
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
	private boolean scroll(double mouseX, double mouseY, double scrollY) {
		if (dialog.open()) return true;
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
	private boolean charType(char codePoint) {
		if (closing || dialog.open()) return true;
		if (capture != null) return true;
		var popup = activePopup();
		if (popup != null && popup.popupCharTyped(ui, codePoint)) return true;
		if (header.searchField().charTyped(ui, codePoint)) return true;
		for (var row : activeRows()) if (row.charTyped(ui, codePoint)) return true;
		return false;
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
		contentPane.ensureVisible(controls.get(next));
		return true;
	}
	private List<ConfigRow> activeRows() {
		List<ConfigRow> rows;
		if (search.filtering()) rows = search.rows();
		else {
			var tab = model.activeTab();
			rows = tab == null ? List.of() : tab.rows;
		}
		if (leaving.isEmpty()) return rows;
		var merged = new ArrayList<ConfigRow>(rows.size() + leaving.size());
		merged.addAll(rows);
		for (var item : leaving) {
			if (rows.contains(item.row())) continue;
			var index = item.anchor() == null ? -1 : merged.indexOf(item.anchor());
			merged.add(index < 0 ? merged.size() : index, item.row());
		}
		return merged;
	}
	private void showDialog(Runnable action) {
		closeDropdown();
		capture = null;
		dialog.open(Translation.QUIT_CONFIRM_TITLE, Translation.QUIT_CONFIRM_WARNING, action);
		ui.focus.clear();
	}
	private void beginClose(Runnable action) {
		closing = true;
		closeAction = action;
		closeAnim.start(1, 0, UiMotion.EXIT);
	}
	private void saveAndQuit() {
		if (!session.editable()) return;
		var restartType = session.restartType();
		if (!session.save()) return;
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
				Translation.toComponent(Translation.RESTART_REQUIRED_TITLE),
				Translation.toComponent(Translation.RESTART_REQUIRED_WORLD),
				mc.isLocalServer()
					? Translation.toComponent(Translation.RETURN_TO_MENU)
					: Translation.toComponent(UiText.translatable("menu.disconnect")),
				Translation.toComponent(Translation.IGNORE_RESTART_LABEL)
			);
		}
		return new ConfirmScreen(
			confirmed -> {
				if (confirmed) mc.stop();
				else host.setScreen(host.parentScreen());
			},
			Translation.toComponent(Translation.RESTART_REQUIRED_TITLE),
			Translation.toComponent(Translation.RESTART_REQUIRED_LABEL),
			Translation.toComponent(Translation.QUIT_GAME),
			Translation.toComponent(Translation.IGNORE_RESTART_LABEL)
		);
	}
	private void undoAll() {
		session.resetAll();
		rebuildActiveTab();
		for (var tab : model.tabs()) for (var row : tab.rows) if (row instanceof ValueRow valueRow) valueRow.wash();
		toast.info(Translation.TOAST_UNDONE);
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
	public void rebuildActiveTab() {
		if (model == null) return;
		closeDropdown();
		dropLeavingRows();
		var hadPending = !removals.isEmpty();
		var expansions = removals.flush();
		// 刚处理过删除/重置时，旧行的收缩比例属于已被移除的项，不能套到同路径的新行上
		var quiet = suppressRebuildFx || hadPending;
		var tab = model.activeTab();
		// 筛选列表横跨所有分类，元素的进入/恢复动画和分页判断都要覆盖全部行
		var oldIds = new HashSet<>();
		var activeOldIds = new HashSet<>();
		var expanding = new HashMap<Object, Float>();
		for (var each : model.tabs())
			for (var row : each.rows) {
				var id = row.identity();
				if (id == null) continue;
				oldIds.add(id);
				if (each == tab) activeOldIds.add(id);
				if (row.heightScale() < 1) expanding.put(id, row.heightScale());
			}
		model.rebuildAll();
		modelVersion++;
		var activeNewIds = new HashSet<>();
		for (var each : model.tabs()) {
			var newIds = new HashSet<>();
			for (var row : each.rows) {
				var id = row.identity();
				if (id != null) newIds.add(id);
			}
			for (var row : each.rows) {
				var id = row.identity();
				if (id == null) continue;
				if (!oldIds.contains(id)) {
					if (!quiet) row.onEnter();
				} else if (!quiet) {
					var scale = expanding.get(id);
					if (scale != null) row.expandFrom(scale);
				}
			}
			if (each == tab) activeNewIds = newIds;
		}
		if (!quiet && tab != null) {
			for (var id : activeOldIds)
				if (!activeNewIds.contains(id)) {
					pageIn.start(0.5F, 1, UiMotion.TAB_ROWS_IN);
					break;
				}
		}
		// 筛选时列表滚动属于筛选结果，不能换成当前分类的存档位置
		if (tab != null && !search.filtering()) {
			tab.clampScroll(layout.content.height(), layout.content.width());
			contentPane.setScroll(tab.scroll);
		}
		for (var path : expansions) animateCollection(path);
		refresh();
	}
	private void done() {
		if (!session.editable() || isActiveValue()) {
			onClose();
			return;
		}
		if (validate() != null || model.hasError()) return;   // 无效值不保存也不关闭
		saveAndQuit();
	}
	private @Nullable UiText doneBlockedReason() {
		if (!session.editable()) return Translation.tooltip(Translation.SAVE_UNAVAILABLE, Translation.SAVE_UNAVAILABLE_READ_ONLY);
		if (validate() != null || model.hasError())
			return Translation.tooltip(Translation.SAVE_UNAVAILABLE, Translation.SAVE_UNAVAILABLE_INVALID);
		return null;
	}
	private @Nullable UiText validate() {
		return session.validate();
	}
	private boolean isActiveValue() {
		return session.isActiveValue();
	}
	@Override
	public ConfigHandler<?, ?> handler() {
		return session.handler();
	}
	@Override
	public Object config() {
		return session.config();
	}
	@Override
	public FileKey fileKey() {
		return new FileKey(session.root().modId(), session.root().fileName());
	}
	@Override
	public String modId() {
		return session.root().modId();
	}
	@Override
	public boolean editable() {
		return session.editable();
	}
	@Override
	public boolean lockable() {
		return session.lockable();
	}
	@Override
	public void refresh() {
		if (model == null) return;
		model.refreshAll();
		if (doneButton == null) return;
		search.markDirty();
		doneButton.tooltip(doneBlockedReason());
		var changed = model.changedCount();
		header.setUndo(changed, session.editable());
		header.setChanged(model.defaultChangedCount(), search.changedOnly());
	}
	@Override
	public void rebuildTab() {
		rebuildActiveTab();
	}
	@Override
	public void beginCapture(CaptureTarget target) {
		closeDropdown();
		capture = target;
		ui.focus.clear();
	}
	@Override
	public void endCapture(CaptureTarget target) {
		if (capture == target) capture = null;
	}
	@Override
	public void saveByKeybind() {
		session.saveByKeybind();
	}
	@Override
	public void exitRow(Object identity, Runnable action) {
		if (removals.exitRow(identity, activeRows(), action)) rebuildActiveTab();
	}
	/** 收起一个集合的全部元素行，重建后让新的元素行重新展开；用于集合重置。 */
	@Override
	public void exitElements(String collectionPath, Runnable action) {
		removals.exitElements(collectionPath, activeRows(), action);
	}
	@Override
	public void openDropdown(UiDropdown dropdown) {
		if (popups.openDropdown() == dropdown) {
			closeDropdown();
			return;
		}
		popups.open(dropdown, layout.content);
		ui.focus.set(dropdown.filterField());
	}
	@Override
	public void closeDropdown() {
		popups.close();
	}
	private @Nullable UiPopup activePopup() {
		return popups.active();
	}
	private void openPopup(UiPopup popup) {
		popups.open(popup);
	}
}
