package io.github.forgestove.flexconfig.client.gui;
import io.github.forgestove.flexconfig.client.ClientUtil;
import io.github.forgestove.flexconfig.client.gui.api.CrossRefreshable;
import io.github.forgestove.flexconfig.client.gui.components.FlexScrollBar;
import io.github.forgestove.flexconfig.client.gui.entry.ConfigEntry;
import io.github.forgestove.flexconfig.client.gui.factory.Highlight;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public final class ConfigEntryList extends ContainerObjectSelectionList<ConfigEntry> {
	private final Screen screen;
	private final Highlight highlight = new Highlight(() -> children().indexOf(getHovered()), this::getRowTop);
	private final FlexScrollBar scrollbar = new FlexScrollBar();
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
		return width * 3 / 5;
	}
	@Override
	public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
		scrollbar.area(getScrollbarPosition(), getY(), height, getMaxScroll() + height);
		scrollbar.update();
		setScrollAmount(scrollbar.scroll());
		highlight.tick(gui, getX(), getY(), width, height, itemHeight, delta);
		super.renderWidget(gui, mouseX, mouseY, delta);
		scrollbar.render(gui, mouseX, mouseY, delta);
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
	/** 列表背景透明：不铺任何底色，露出屏后的世界（原版模糊背景）。 */
	@Override
	protected void renderListBackground(@NotNull GuiGraphics gui) {
	}
	/** 关掉原版滚动条，改由 {@link FlexScrollBar} 画。 */
	@Override
	protected boolean scrollbarVisible() {
		return false;
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (scrollbar.mouseClicked(mouseX, mouseY, button)) return true;
		return super.mouseClicked(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		scrollbar.mouseReleased(mouseX, mouseY, button);
		return super.mouseReleased(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (scrollbar.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
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
}
