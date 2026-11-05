package io.github.forgestove.flexconfig.client.gui.im.model;
import io.github.forgestove.flexconfig.client.gui.im.row.*;
import io.github.forgestove.flexconfig.tree.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/** 把配置树展开成行；分类展开状态在屏幕之间保持。 */
public class RowBuilder {
	private static final Set<CategoryConfigNode<?>> expanded = new HashSet<>();
	private static final Set<CategoryConfigNode<?>> defaultsApplied = new HashSet<>();
	public static void buildInto(RowContext ctx, CategoryConfigNode<?> root, List<ConfigRow> out) {
		if (defaultsApplied.add(root)) collectDefaults(root);
		addChildren(ctx, root, 0, out);
	}
	private static void addChildren(RowContext ctx, CategoryConfigNode<?> category, int depth, List<ConfigRow> out) {
		for (var child : category.getChildren()) addChild(ctx, child, depth, out);
	}
	private static void addChild(RowContext ctx, ConfigNode<?> child, int depth, List<ConfigRow> out) {
		if (child instanceof ValueConfigNode<?, ?> valueNode) {
			if (valueNode.getValueType() == List.class) {
				CollectionRows.append(ctx, valueNode, depth, out);
				return;
			}
			var row = RowTypeRegistry.create(ctx, valueNode);
			row.setDepth(depth);
			out.add(row);
			return;
		}
		if (child instanceof CategoryConfigNode<?> sub) {
			var open = expanded.contains(sub);
			var toggle = (Runnable) () -> {
				if (!expanded.remove(sub)) expanded.add(sub);
				ctx.rebuildTab();
			};
			out.add(new SectionRow(ctx, sub, depth, toggle));
			if (open) addChildren(ctx, sub, depth + 1, out);
		}
	}
	private static void collectDefaults(CategoryConfigNode<?> category) {
		for (var child : category.getChildren()) {
			if (!(child instanceof CategoryConfigNode<?> sub)) continue;
			if (sub.isDefaultExpanded()) expanded.add(sub);
			collectDefaults(sub);
		}
	}
}
