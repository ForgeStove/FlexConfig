package io.github.forgestove.flexconfig.client.gui.popup;
import io.github.forgestove.flexconfig.client.gui.FlexGuiTextures;
import io.github.forgestove.flexconfig.client.gui.components.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.*;
/**
 * 列表弹窗：一列可滚动的文字项，悬停铺灰底、当前项铺主题绿。
 * <p>
 * 泛化到任意值类型：枚举下拉（值 = 枚举常量、回调写回）与上下文菜单（值 = 动作名、回调执行）都用它。
 */
public class OreListPopupScreen<T> extends OrePopupScreen {
	/** 悬停 / 当前项配色（当前项对齐基岩列表的选中绿）。 */
	private static final int ROW_HOVER = 0xFF56585B, ROW_SELECTED = 0xFF3C8527, TEXT = 0xFFFFFFFF;	/** 行高、行间距、行内左右留白。 */
	private static final int ROW_H = 14, ROW_GAP = 1, ITEM_H = ROW_H + ROW_GAP, CELL_PAD = 5;
	/** 顶部搜索框高度。 */
	private static final int SEARCH_H = 16;
	private final List<T> options;
	private final Function<T, Component> display;
	private final Supplier<T> current;
	private final Consumer<T> onSelect;
	private final boolean searchable;
	private final OreScrollbar scrollbar = new OreScrollbar();
	@Nullable private OreEditBox search;
	/** 过滤后的当前列表；无搜索框时恒等于 {@link #options}。 */
	private List<T> filtered;
	/** 可见行数与是否超出一屏。 */
	private int visible;
	private boolean scrollable;
	public OreListPopupScreen(
		@NotNull Screen parent,
		@NotNull List<T> options,
		@NotNull Function<T, Component> display,
		@NotNull Supplier<T> current,
		@NotNull Consumer<T> onSelect,
		boolean searchable
	) {
		this(parent, null, options, display, current, onSelect, searchable);
	}
	public OreListPopupScreen(
		@NotNull Screen parent,
		@Nullable Component title,
		@NotNull List<T> options,
		@NotNull Function<T, Component> display,
		@NotNull Supplier<T> current,
		@NotNull Consumer<T> onSelect,
		boolean searchable
	) {
		super(parent, title);
		this.options = options;
		this.display = display;
		this.current = current;
		this.onSelect = onSelect;
		this.searchable = searchable;
		filtered = options;
		scrollbar.step(ITEM_H);
	}
	@Override
	protected void buildWidgets() {
		if (!searchable) return;
		search = new OreEditBox(font, 0, 0, contentW(), SEARCH_H, Component.empty());
		search.setResponder(text -> {
			refilter();
			scrollbar.seek(0);
			relayout();
		});
		addRenderableWidget(search);
	}
	/** 按搜索词过滤；空词即全部。 */
	private void refilter() {
		var query = search == null ? "" : search.getValue().trim().toLowerCase(Locale.ROOT);
		filtered = query.isEmpty()
			? options
			: options.stream().filter(option -> display.apply(option).getString().toLowerCase(Locale.ROOT).contains(query)).toList();
	}
	@Override
	protected void init() {
		super.init();
		// 打开时把当前项摆到中间
		var index = options.indexOf(current.get());
		if (index > 0) scrollbar.seek(index * ITEM_H - visible * ITEM_H / 2.0);
	}
	/**
	 * 尺寸在这里算（{@code relayout} 先问宽、后问高）：可见行数要同时受屏幕全高与**锚点下方**空间限制，
	 * 否则下拉会盖住触发它的控件或越出屏外。
	 */
	@Override
	protected int contentWidth() {
		var extra = searchable ? SEARCH_H + ROW_GAP : 0;
		var limit = Math.max(1, (height - PAD * 2 - extra) / ITEM_H);
		var below = anchorBottom();
		if (below >= 0) limit = Math.max(1, Math.clamp((height - below - PAD * 2 - extra) / ITEM_H, 1, limit));
		visible = Math.clamp(filtered.size(), 1, limit);
		scrollable = filtered.size() > visible;
		var width = 0;
		// 宽度按整组算，不随过滤结果收缩，否则搜出两三条时弹窗会忽宽忽窄
		for (var option : options) width = Math.max(width, font.width(display.apply(option)));
		return width + CELL_PAD * 2 + (scrollable ? scrollbarWidth() : 0);
	}
	/** 滚动条宽 = 滑块精灵宽（随资源包变）。 */
	private int scrollbarWidth() {
		return FlexGuiTextures.SCROLLBAR_TOP.width();
	}
	@Override
	protected int contentHeight() {
		return visible * ITEM_H + (searchable ? SEARCH_H + ROW_GAP : 0);
	}
	@Override
	protected void layoutContent() {
		scrollbar.area(contentX() + contentW() - scrollbarWidth(), listY(), listH(), filtered.size() * ITEM_H);
		if (search == null) return;
		search.setRectangle(contentW(), SEARCH_H, contentX(), contentY());
	}
	/** 列表区顶端（搜索行之下）。 */
	private int listY() {
		return contentY() + (searchable ? SEARCH_H + ROW_GAP : 0);
	}
	/** 列表区高度。 */
	private int listH() {
		return contentH() - (searchable ? SEARCH_H + ROW_GAP : 0);
	}
	@Override
	protected void renderContent(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		scrollbar.dragTo(mouseY);   // 拖动落位：按每帧真实鼠标坐标推进，不用 mouseDragged
		scrollbar.update();
		var x = contentX();
		var y = listY();
		var width = listW();
		var currentValue = current.get();
		var scroll = scrollbar.scroll();
		gui.enableScissor(contentX(), y, contentX() + contentW(), y + listH());
		for (var i = 0; i < filtered.size(); i++) {
			var rowY = (int) Math.round(y + i * ITEM_H - scroll);
			if (rowY + ROW_H < y || rowY > y + listH()) continue;
			var option = filtered.get(i);
			var hovered = mouseX >= x && mouseX < x + width && mouseY >= rowY && mouseY < rowY + ROW_H;
			if (Objects.equals(option, currentValue)) gui.fill(x, rowY, x + width, rowY + ROW_H, ROW_SELECTED);
			else if (hovered) gui.fill(x, rowY, x + width, rowY + ROW_H, ROW_HOVER);
			gui.drawString(font, display.apply(option).getVisualOrderText(), x + CELL_PAD, rowY + (ROW_H - 8) / 2, TEXT, true);
		}
		gui.disableScissor();
		scrollbar.render(gui, mouseX, mouseY);
		if (rowAt(mouseX, mouseY) >= 0) OreCursor.hand();
	}
	/** 列表区宽度 = 内容宽 − 滚动条留位。 */
	private int listW() {
		return contentW() - (scrollable ? scrollbarWidth() : 0);
	}
	/** @return 鼠标所在行号；不在列表区内返回 -1。 */
	private int rowAt(double mouseX, double mouseY) {
		var x = contentX();
		if (mouseX < x || mouseX >= x + listW()) return -1;
		var y = listY();
		if (mouseY < y || mouseY >= y + listH()) return -1;
		var index = (int) ((mouseY - y + scrollbar.scroll()) / ITEM_H);
		return index >= 0 && index < filtered.size() ? index : -1;
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (super.keyPressed(keyCode, scanCode, modifiers)) return true;   // 聚焦的搜索框先吃退格
		if (search != null && hasControlDown() && keyCode == GLFW.GLFW_KEY_F) {
			setFocused(search);
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_BACKSPACE) onClose();
		return false;
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!isOutsidePanel(mouseX, mouseY)) {
			if (scrollbar.mousePressed(mouseX, mouseY)) return true;
			var index = rowAt(mouseX, mouseY);
			if (index >= 0) {
				// 先关（回父界面）再执行选中回调：回调里可以直接跳到别的界面，不会被 onClose 覆盖
				onClose();
				onSelect.accept(filtered.get(index));
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		scrollbar.release();
		return super.mouseReleased(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		if (isOutsidePanel(mouseX, mouseY)) return true;
		scrollbar.wheel(vertical);
		return true;
	}

}
