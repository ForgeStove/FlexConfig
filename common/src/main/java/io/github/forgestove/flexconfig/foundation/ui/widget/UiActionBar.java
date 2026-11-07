package io.github.forgestove.flexconfig.foundation.ui.widget;
import io.github.forgestove.flexconfig.foundation.ui.core.*;

import java.util.*;
/** 行尾控件从左往右排列，宽度由调用方决定。 */
public class UiActionBar {
	private final List<Entry> entries = new ArrayList<>();
	public UiActionBar add(UiControl control, int width) {
		entries.add(new Entry(control, width));
		return this;
	}
	public List<UiControl> controls() {
		var list = new ArrayList<UiControl>(entries.size());
		for (var entry : entries) if (entry.control().visible()) list.add(entry.control());
		return list;
	}
	/** 始终为隐藏的 reset/undo 预留宽度，避免悬停时标题和描述跳动。 */
	public int usedWidth() {
		var width = 0;
		for (var entry : entries) width += entry.width();
		return entries.isEmpty() ? 0 : width + (entries.size() - 1) * UiTheme.GAP;
	}
	public void layout(int right, int rowY, int rowHeight) {
		var cursor = right - usedWidth();
		var height = Math.min(rowHeight, UiTheme.CONTROL_HEIGHT);
		for (var entry : entries) {
			var control = entry.control();
			control.setBounds(new UiBounds(cursor, rowY + (rowHeight - height) / 2, entry.width(), height));
			cursor += entry.width() + UiTheme.GAP;
		}
	}
	public void render(UiContext ctx) {
		for (var entry : entries) entry.control().render(ctx);
	}
	public boolean mouseClicked(UiContext ctx, double mouseX, double mouseY, int button) {
		for (var entry : entries) if (entry.control().mouseClicked(ctx, mouseX, mouseY, button)) return true;
		return false;
	}
	public boolean mouseReleased(UiContext ctx, double mouseX, double mouseY, int button) {
		for (var entry : entries) if (entry.control().mouseReleased(ctx, mouseX, mouseY, button)) return true;
		return false;
	}
	private record Entry(UiControl control, int width) {}
}
