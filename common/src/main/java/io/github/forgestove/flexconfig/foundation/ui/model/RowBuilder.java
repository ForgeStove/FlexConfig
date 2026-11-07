package io.github.forgestove.flexconfig.foundation.ui.model;
import io.github.forgestove.flexconfig.foundation.ui.row.*;
import io.github.forgestove.flexconfig.foundation.tree.*;

import java.util.*;
public class RowBuilder {
	public static void buildInto(RowContext ctx, CategoryConfigNode root, List<ConfigRow> out) {
		addChildren(ctx, root, 0, out);
	}
	private static void addChildren(RowContext ctx, CategoryConfigNode category, int depth, List<ConfigRow> out) {
		for (var child : category.getChildren()) addChild(ctx, child, depth, out);
	}
	private static void addChild(RowContext ctx, ConfigNode child, int depth, List<ConfigRow> out) {
		if (child instanceof ValueConfigNode valueNode) {
			if (valueNode.getValueType() == List.class) {
				CollectionRows.append(ctx, valueNode, depth, out);
				return;
			}
			var row = RowTypeRegistry.create(ctx, valueNode);
			row.setDepth(depth);
			out.add(row);
			return;
		}
		if (child instanceof CategoryConfigNode sub) {
			out.add(new SectionRow(ctx, sub, depth));
			addChildren(ctx, sub, depth + 1, out);
		}
	}
}
