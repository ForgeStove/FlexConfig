package io.github.forgestove.flexconfig.foundation.ui.model;
import io.github.forgestove.flexconfig.foundation.ui.row.*;

import java.util.*;
/** 跨 tab 搜索：匹配标题、tooltip 与配置路径，可选只看修改项和限定当前 tab。 */
public class SearchIndex {
	public static List<ConfigRow> search(RowContext ctx, ConfigUiModel model, String query, boolean changedOnly) {
		var needle = query.trim().toLowerCase();
		var out = new ArrayList<ConfigRow>();
		if (needle.isEmpty() && !changedOnly) return out;
		for (var tab : model.tabs()) {
			var matches = new ArrayList<ValueRow>();
			for (var row : tab.rows) {
				if (!(row instanceof ValueRow valueRow)) continue;
				if (changedOnly && !row.isChanged()) continue;
				var node = valueRow.node();
				var title = node.getTitle().getString();
				var path = node.getPath();
				var tooltip = node.getTooltip() == null ? "" : node.getTooltip().getString();
				if (title.toLowerCase().contains(needle) || path.toLowerCase().contains(needle) || tooltip.toLowerCase().contains(needle)) {
					valueRow.setHighlight(needle);
					matches.add(valueRow);
				}
			}
			if (matches.isEmpty()) continue;
			out.add(new SearchHeaderRow(ctx, tab.title, 0));
			out.addAll(matches);
		}
		return out;
	}
	public static void clearHighlights(ConfigUiModel model) {
		for (var tab : model.tabs()) for (var row : tab.rows) if (row instanceof ValueRow valueRow) valueRow.setHighlight("");
	}
}
