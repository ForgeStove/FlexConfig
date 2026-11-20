package io.github.forgestove.flexconfig.foundation.ui.model;
import io.github.forgestove.flexconfig.foundation.client.Translation;
import io.github.forgestove.flexconfig.foundation.tree.CategoryConfigNode;
import io.github.forgestove.flexconfig.foundation.ui.core.UiText;
import io.github.forgestove.flexconfig.foundation.ui.row.ConfigRow;
import net.minecraft.util.Mth;

import java.util.*;
public class ConfigTabModel implements ConfigStatus {
	public final CategoryConfigNode node;
	public final UiText title;
	public final List<ConfigRow> rows = new ArrayList<>();
	public double scroll;
	private boolean error;
	public int changedCount;
	public int defaultChangedCount;
	public ConfigTabModel(CategoryConfigNode node) {
		this.node = node;
		title = Translation.from(node.getTitle());
	}
	public void build(RowContext ctx) {
		rows.clear();
		RowBuilder.buildInto(ctx, node, rows);
		refresh();
	}
	public void refresh() {
		error = false;
		changedCount = 0;
		defaultChangedCount = 0;
		for (var row : rows) row.refresh();
		KeybindConflictIndex.apply(rows);
		for (var row : rows) {
			if (row instanceof KeybindConflictAware) row.refresh();
			if (row.hasError()) error = true;
			if (row.isChanged()) changedCount++;
			if (!row.isDefaultValue()) defaultChangedCount++;
		}
	}
	@Override
	public boolean hasError() {
		return error;
	}
	@Override
	public boolean isChanged() {
		return changedCount > 0;
	}
	@Override
	public boolean isDefaultValue() {
		return defaultChangedCount == 0;
	}
	public int contentHeight(int width) {
		var height = 0;
		for (var row : rows) height += row.heightFor(width);
		return height;
	}
	public void clampScroll(int viewportHeight, int width) {
		var max = Math.max(0, contentHeight(width) - viewportHeight);
		scroll = Mth.clamp(scroll, 0, max);
	}
}
