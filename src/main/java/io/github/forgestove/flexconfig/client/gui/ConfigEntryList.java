package io.github.forgestove.flexconfig.client.gui;
import io.github.forgestove.flexconfig.client.ClientUtil;
import io.github.forgestove.flexconfig.client.gui.api.CrossRefreshable;
import io.github.forgestove.flexconfig.client.gui.components.*;
import io.github.forgestove.flexconfig.client.gui.entry.ConfigEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
public final class ConfigEntryList extends ContainerObjectSelectionList<ConfigEntry> implements ScrollTarget {
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
	/** 列表背景透明：不铺任何底色，露出屏后的世界（原版模糊背景）。 */
	@Override
	protected void renderListBackground(@NotNull GuiGraphics gui) {}
	@Override
	public int getMaxScroll() {
		return Math.max(0, super.getMaxScroll() - ConfigEntry.GAP / 2);
	}
	/** 列表自身不滚：有溢出时滚轮由滚动条接管，没溢出时也不该出现原版那几像素的余量滚动。 */
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
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
	/** 列表的装饰控件（高亮、滚动条）与自身一起交给 tab 注册。 */
	public void visitChildren(Consumer<AbstractWidget> consumer) {
		consumer.accept(highlight);
		consumer.accept(scrollbar);
		consumer.accept(this);
	}
	/** 列表被摆放到最终位置时一并摆好装饰控件。 */
	@Override
	public void setRectangle(int width, int height, int x, int y) {
		super.setRectangle(width, height, x, y);
		scrollbar.setX(getScrollbarPosition());
		scrollbar.setY(getY());
		scrollbar.setWidth(6);
		scrollbar.setHeight(height);
		scrollbar.wheelArea(getRectangle());
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
