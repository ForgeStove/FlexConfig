package io.github.forgestove.flexconfig.client.gui;
import io.github.forgestove.flexconfig.client.ClientUtil;
import io.github.forgestove.flexconfig.client.gui.api.CrossRefreshable;
import io.github.forgestove.flexconfig.client.gui.api.OverlayDropdown;
import io.github.forgestove.flexconfig.client.gui.components.*;
import io.github.forgestove.flexconfig.client.gui.entry.ConfigEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
public class ConfigEntryList extends ContainerObjectSelectionList<ConfigEntry> implements ScrollTarget {
	private final Screen screen;
	private final FlexHighlight highlight;
	private final FlexScrollBar scrollbar;
	public ConfigEntryList(@NotNull ConfigScreen<?, ?> screen, @NotNull Iterable<ConfigEntry> entries) {
		super(
			ClientUtil.mc,
			screen.width,
			screen.height - screen.getHeaderHeight() - screen.getFooterHeight(),
			screen.getHeaderHeight(),
			ConfigEntry.HEIGHT + ConfigEntry.GAP
		);
		this.screen = screen;
		headerHeight = -3;
		highlight = new FlexHighlight(itemHeight, this::hoveredIndex, this::getRowTop);
		scrollbar = new FlexScrollBar(this, getScrollbarPosition(), getY(), 6, height);
		scrollbar.step(itemHeight);
		entries.forEach(this::addEntry);
	}
	public void refresh() {
		for (var entry : children())
			if (entry instanceof CrossRefreshable refreshable && refreshable.beginCrossEntryRefresh(children())) break;
		children().forEach(ConfigEntry::refresh);
	}
	public boolean hasEntryError() {
		for (var configEntry : children()) if (configEntry.hasError()) return true;
		return false;
	}
	public void replaceAllEntries(@NotNull List<ConfigEntry> newEntries) {
		children().clear();
		newEntries.forEach(this::addEntry);
	}
	@Override
	public int getRowWidth() {
		return width * 2 / 3;
	}
	@Override
	public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
		super.renderWidget(gui, mouseX, mouseY, delta);
		// Cloth 式二次绘制：先把已排队的行背景/文字在列表 scissor 下 flush，再画展开菜单
		gui.enableScissor(getX(), getY(), getX() + getWidth(), getY() + getHeight());
		gui.flush();
		var children = children();
		for (var i = 0; i < children.size(); i++) {
			if (getRowBottom(i) < getY() || getRowTop(i) > getBottom()) continue;
			children.get(i).lateRender(gui, mouseX, mouseY, delta);
		}
		gui.disableScissor();
		if (isOverOpenDropdown(mouseX, mouseY)) return;
		var entry = getHovered();
		if (entry == null) return;
		var widgetTooltip = entry.getHoveredWidgetTooltip(mouseX, mouseY);
		if (widgetTooltip != null) {
			screen.setTooltipForNextRenderPass(widgetTooltip.toCharSequence(minecraft));
			return;
		}
		var tooltip = entry.getTooltip();
		if (tooltip != null) screen.setTooltipForNextRenderPass(tooltip);
	}
	/** 关掉原版滚动条，改由 {@link FlexScrollBar} 画。 */
	@Override
	protected boolean scrollbarVisible() {
		return false;
	}
	@Override
	public int getMaxScroll() {
		return Math.max(0, super.getMaxScroll() - ConfigEntry.GAP / 2);
	}
	/** 展开的下拉菜单超出本行，需要计入滚动范围（同 Cloth 的 morePossibleHeight）。 */
	@Override
	protected int getMaxPosition() {
		var max = super.getMaxPosition();
		var position = headerHeight;
		for (var entry : children()) {
			position += itemHeight;
			var extra = entry.getMorePossibleHeight();
			if (extra >= 0) max = Math.max(max, position + extra);
		}
		return max;
	}
	private boolean isOverOpenDropdown(double mouseX, double mouseY) {
		for (var entry : children())
			if (entry instanceof OverlayDropdown dropdown && dropdown.isDropdownOpen() && dropdown.isMouseOverDropdown(mouseX, mouseY))
				return true;
		return false;
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		for (var entry : children()) {
			if (!(entry instanceof OverlayDropdown dropdown) || !dropdown.isDropdownOpen()) continue;
			if (dropdown.isMouseOverDropdown(mouseX, mouseY)) return dropdown.dropdownMouseClicked(mouseX, mouseY, button);
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	/** 原版的滚动条区域拖拽判定关掉：滚动条由 {@link FlexScrollBar} 自己处理。 */
	@Override
	protected void updateScrollingState(double mouseX, double mouseY, int button) {}
	/** 滚轮转交给滚动条；没溢出时滚动条不响应，列表自身也不滚。 */
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		for (var entry : children())
			if (entry instanceof OverlayDropdown dropdown && dropdown.isDropdownOpen() && dropdown.isMouseOverDropdown(mouseX, mouseY))
				return dropdown.dropdownMouseScrolled(mouseX, mouseY, vertical);
		scrollbar.wheel(vertical);
		return true;
	}
	@Override
	protected void renderListItems(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
		var left = getRowLeft();
		var width = getRowWidth();
		var count = getItemCount();
		for (var i = 0; i < count; i++) {
			var top = getRowTop(i);
			var bottom = getRowBottom(i);
			if (bottom >= getY() && top <= getBottom()) renderItem(gui, mouseX, mouseY, delta, i, left, top, width, itemHeight);
		}
	}
	/** 列表的装饰控件（高亮、滚动条）与自身一起交给 tab 注册；滚动条排在最后，画在列表之上。 */
	public void visitChildren(Consumer<AbstractWidget> consumer) {
		consumer.accept(highlight);
		consumer.accept(this);
		consumer.accept(scrollbar);
	}
	/** 列表被摆放到最终位置时一并摆好装饰控件。 */
	@Override
	public void setRectangle(int width, int height, int x, int y) {
		super.setRectangle(width, height, x, y);
		scrollbar.setX(getScrollbarPosition());
		scrollbar.setY(getY());
		scrollbar.setWidth(6);
		scrollbar.setHeight(height);
		highlight.setX(x);
		highlight.setY(y);
		highlight.setWidth(width);
		highlight.setHeight(height);
	}
	@Override
	public int contentHeight() {
		return getMaxPosition();
	}
	@Override
	public int maxScroll() {
		return getMaxScroll();
	}
	@Override
	public void applyScroll(double scroll) {
		setScrollAmount(scroll);
	}
	/** 当前鼠标下的行号；{@link FlexHighlight} 每帧按它定位。鼠标离开列表矩形时返回 -1。 */
	public int hoveredIndex(double mouseX, double mouseY) {
		if (!isMouseOver(mouseX, mouseY)) return -1;
		var entry = getEntryAtPosition(mouseX, mouseY);
		return entry == null ? -1 : children().indexOf(entry);
	}
}
