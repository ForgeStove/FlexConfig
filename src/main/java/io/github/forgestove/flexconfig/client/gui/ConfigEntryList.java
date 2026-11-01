package io.github.forgestove.flexconfig.client.gui;
import io.github.forgestove.flexconfig.client.ClientUtil;
import io.github.forgestove.flexconfig.client.gui.api.CrossRefreshable;
import io.github.forgestove.flexconfig.client.gui.components.*;
import io.github.forgestove.flexconfig.client.gui.entry.ConfigEntry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
//? if <1.20.2 {
/*import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
*///?}
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
public final class ConfigEntryList extends ContainerObjectSelectionList<ConfigEntry> implements ScrollTarget {
	private final Screen screen;
	private final FlexHighlight highlight;
	private final FlexScrollBar scrollbar;
	//? if <1.20.2 {
	/*private final ListWidget widget;
	*///?}
	public ConfigEntryList(@NotNull ConfigScreen<?, ?> screen, @NotNull Iterable<ConfigEntry> entries) {
		//? if >=1.20.2 {
		super(
			ClientUtil.mc,
			screen.width,
			screen.height - screen.getHeaderHeight() - screen.getFooterHeight(),
			screen.getHeaderHeight(),
			ConfigEntry.HEIGHT + ConfigEntry.GAP
		);
		//?} else {
		/*// 1.20.1 的构造器分上下边界，高度仍是整屏高
		super(
			ClientUtil.mc,
			screen.width,
			screen.height,
			screen.getHeaderHeight(),
			screen.height - screen.getFooterHeight(),
			ConfigEntry.HEIGHT + ConfigEntry.GAP
		);
		*///?}
		this.screen = screen;
		headerHeight = -3;
		highlight = new FlexHighlight(itemHeight, this::hoveredIndex, this::getRowTop);
		//? if >=1.20.2 {
		scrollbar = new FlexScrollBar(this, getScrollbarPosition(), getY(), 6, height);
		//?} else {
		/*scrollbar = new FlexScrollBar(this, getScrollbarPosition(), getTop(), 6, getBottom() - getTop());
		widget = new ListWidget(this);
		*///?}
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
	//? if >=1.20.2 {
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
	//?} else {
	/*@Override
	public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
		super.render(gui, mouseX, mouseY, delta);
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
	*///?}
	/** 关掉原版滚动条，改由 {@link FlexScrollBar} 画。 */
	//? if >=1.20.2 {
	@Override
	protected boolean scrollbarVisible() {
		return false;
	}
	//?}
	@Override
	public int getMaxScroll() {
		return Math.max(0, super.getMaxScroll() - ConfigEntry.GAP / 2);
	}
	/** 原版的滚动条区域拖拽判定关掉：滚动条由 {@link FlexScrollBar} 自己处理。 */
	@Override
	protected void updateScrollingState(double mouseX, double mouseY, int button) {}
	/** 滚轮转交给滚动条；没溢出时滚动条不响应，列表自身也不滚。 */
	//? if >=1.20.2 {
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		scrollbar.wheel(vertical);
		return true;
	}
	//?} else {
	/*@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double vertical) {
		scrollbar.wheel(vertical);
		return true;
	}
	*///?}
	//? if >=1.20.2 {
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
	//?} else {
	/*@Override
	protected void renderList(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
		var left = getRowLeft();
		var width = getRowWidth();
		var count = getItemCount();
		for (var i = 0; i < count; i++) {
			var top = getRowTop(i);
			var bottom = getRowBottom(i);
			if (bottom >= getTop() && top <= getBottom()) renderItem(gui, mouseX, mouseY, delta, i, left, top, width, itemHeight);
		}
	}
	*///?}
	/** 列表的装饰控件（高亮、滚动条）与自身一起交给 tab 注册；滚动条排在最后，画在列表之上。 */
	public void visitChildren(Consumer<AbstractWidget> consumer) {
		consumer.accept(highlight);
		//? if >=1.20.2 {
		consumer.accept(this);
		//?} else {
		/*consumer.accept(widget);
		*///?}
		consumer.accept(scrollbar);
	}
	/** 列表被摆放到最终位置时一并摆好装饰控件。 */
	//? if >=1.20.2 {
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
	//?} else {
	/*public void setRectangle(int width, int height, int x, int y) {
		updateSize(width, height, y, y + height);
		setLeftPos(x);
		widget.setX(x);
		widget.setY(y);
		widget.setWidth(width);
		widget.setHeight(height);
		scrollbar.setX(getScrollbarPosition());
		scrollbar.setY(getTop());
		scrollbar.setWidth(6);
		scrollbar.setHeight(height);
		highlight.setX(x);
		highlight.setY(y);
		highlight.setWidth(width);
		highlight.setHeight(height);
	}
	*///?}
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
	//? if <1.20.2 {
	/*/^* 1.20.1 的列表不是控件，而 Tab 只接受控件，这里包一层把事件转给列表。 ^/
	private static final class ListWidget extends AbstractWidget {
		private final ConfigEntryList list;
		private ListWidget(ConfigEntryList list) {
			super(0, 0, 0, 0, Component.empty());
			this.list = list;
		}
		@Override
		protected void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
			list.render(gui, mouseX, mouseY, delta);
		}
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			return list.mouseClicked(mouseX, mouseY, button);
		}
		@Override
		public boolean mouseReleased(double mouseX, double mouseY, int button) {
			return list.mouseReleased(mouseX, mouseY, button);
		}
		@Override
		public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
			return list.mouseDragged(mouseX, mouseY, button, dragX, dragY);
		}
		@Override
		public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
			return list.mouseScrolled(mouseX, mouseY, delta);
		}
		@Override
		public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
			return list.keyPressed(keyCode, scanCode, modifiers);
		}
		@Override
		public boolean isMouseOver(double mouseX, double mouseY) {
			return list.isMouseOver(mouseX, mouseY);
		}
		@Override
		@Nullable
		public ComponentPath nextFocusPath(FocusNavigationEvent event) {
			return list.nextFocusPath(event);
		}
		@Override
		protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {}
	}
	*///?}
}
