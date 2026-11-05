package io.github.forgestove.flexconfig.client.gui.im.model;
import io.github.forgestove.flexconfig.client.gui.im.core.UiMath;
import io.github.forgestove.flexconfig.client.gui.im.row.ConfigRow;
import io.github.forgestove.flexconfig.tree.CategoryConfigNode;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;
public class ConfigTabModel {
	public final CategoryConfigNode<Object> node;
	public final Component title;
	public final List<ConfigRow> rows = new ArrayList<>();
	public double scroll;
	public boolean hasError;
	public int changedCount;
	@SuppressWarnings("unchecked")
	public ConfigTabModel(CategoryConfigNode<?> node) {
		this.node = (CategoryConfigNode<Object>) node;
		title = node.getTitle();
	}
	public void build(RowContext ctx) {
		rows.clear();
		RowBuilder.buildInto(ctx, node, rows);
		refresh();
	}
	public void refresh() {
		hasError = false;
		changedCount = 0;
		for (var row : rows) row.refresh();
		KeybindConflictIndex.apply(rows);
		for (var row : rows) {
			if (row instanceof KeybindConflictAware) row.refresh();
			if (row.hasError()) hasError = true;
			if (row.isChanged()) changedCount++;
		}
	}
	public int contentHeight(int width) {
		var height = 0;
		for (var row : rows) height += row.heightFor(width);
		return height;
	}
	public void clampScroll(int viewportHeight, int width) {
		var max = Math.max(0, contentHeight(width) - viewportHeight);
		scroll = UiMath.clamp(scroll, 0, max);
	}
}
