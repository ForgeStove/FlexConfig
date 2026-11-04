package io.github.forgestove.flexconfig.client.gui.entry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.ContainerObjectSelectionList.Entry;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;
public abstract class ConfigEntry extends Entry<ConfigEntry> {
	public static final int SIZE = 20;
	public static final int WIDTH = 100;
	public static final int HEIGHT = 20;
	public static final int GAP = 2;
	public static final int INDENT_PX = 10;
	private int indent;
	public int getIndent() {
		return indent;
	}
	public void setIndent(int indent) {
		this.indent = indent;
	}
	@Nullable
	public List<FormattedCharSequence> getTooltip() {
		return null;
	}
	@Nullable
	public Tooltip getHoveredWidgetTooltip(int mouseX, int mouseY) {
		for (var child : children())
			if (child instanceof AbstractWidget widget && widget.visible && widget.getRectangle().containsPoint(mouseX, mouseY)) {
				var tooltip = widget.getTooltip();
				if (tooltip == null) continue;
				return tooltip;
			}
		return null;
	}
	public void refresh() {}
	/** 行内容器都画完之后再画：展开的下拉菜单在这里覆盖后续条目。 */
	public void lateRender(GuiGraphics gui, int mouseX, int mouseY, float delta) {}
	/** 行外额外需要的高度（如展开的下拉菜单）；{@code -1} 表示没有。 */
	public int getMorePossibleHeight() {
		return -1;
	}
	public boolean hasError() {
		return false;
	}
}
