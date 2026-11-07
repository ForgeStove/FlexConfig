package io.github.forgestove.flexconfig.foundation.ui.model;
import io.github.forgestove.flexconfig.foundation.ConfigTranslation;
import io.github.forgestove.flexconfig.foundation.ElementType;
import io.github.forgestove.flexconfig.foundation.FieldAccess;
import io.github.forgestove.flexconfig.foundation.client.ClientLockManager;
import io.github.forgestove.flexconfig.foundation.ui.core.UiTheme;
import io.github.forgestove.flexconfig.foundation.ui.row.*;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiButton;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiButton.Style;
import io.github.forgestove.flexconfig.foundation.tree.ValueConfigNode;
import io.github.forgestove.flexconfig.foundation.tree.ValueConfigNode.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.*;
/** 集合标题行与元素行的生成。 */
public class CollectionRows {
	public static void append(RowContext ctx, ValueConfigNode node, int depth, List<ConfigRow> out) {
		var header = new CollectionHeaderRow(ctx, node);
		header.setDepth(depth);
		out.add(header);
		appendElements(ctx, node, depth + 1, out);
	}
	public static @Nullable Object defaultValueOf(Class<?> type) {
		type = FieldAccess.boxed(type);
		if (type == Boolean.class) return false;
		if (type == Integer.class) return 0;
		if (type == Long.class) return 0L;
		if (type == Float.class) return 0F;
		if (type == Double.class) return 0D;
		if (type == Byte.class) return (byte) 0;
		if (type == Short.class) return (short) 0;
		if (type == Character.class) return '\0';
		if (type == String.class) return "";
		if (type.isEnum()) {
			var constants = type.getEnumConstants();
			return constants.length == 0 ? null : constants[0];
		}
		return FieldAccess.newInstance(type);
	}
	private static List<Object> values(ValueConfigNode node, RowContext ctx) {
		var value = node.getEditingElements(ctx.config());
		if (value != null) return value;
		value = new ArrayList<>();
		node.setEditingElements(value);
		return value;
	}
	@Nullable
	private static Object defaultElement(ValueConfigNode node, int index) {
		var defaults = node.getDefaultValue();
		if (defaults instanceof List<?> list && index < list.size()) return FieldAccess.deepCopy(list.get(index));
		var shape = node.getElementType();
		return shape == null || shape.isCollection() ? null : defaultValueOf(shape.raw());
	}
	public static void add(RowContext ctx, ValueConfigNode node) {
		var shape = node.getElementType();
		if (shape == null || isReadOnly(ctx, node)) return;
		var template = defaultElement(node, 0);
		var element = shape.isCollection()
			? newNestedElement(shape)
			: shape.raw().isInstance(template) ? FieldAccess.deepCopy(template) : defaultValueOf(shape.raw());
		if (element == null) return;
		var list = values(node, ctx);
		list.add(element);
		node.setEditingElements(list);
		ctx.rebuildTab();
	}
	private static Object newNestedElement(ElementType shape) {
		return shape.isArray() ? FieldAccess.listToArray(shape.raw().getComponentType(), List.of()) : new ArrayList<>();
	}
	public static void remove(RowContext ctx, ValueConfigNode node, int index) {
		if (isReadOnly(ctx, node)) return;
		var list = values(node, ctx);
		if (index < 0 || index >= list.size()) return;
		var element = list.get(index);
		ctx.shell().exitRow(
			elementPath(node, index), () -> {
				// 动画期间列表可能已被别处改动，按元素引用重新定位后再删
				var elements = values(node, ctx);
				var current = indexOfIdentity(elements, element);
				if (current < 0) return;
				ctx.handler().shiftElementKeybinds(node.getPath(), current);
				elements.remove(current);
			}
		);
	}
	private static String elementPath(ValueConfigNode node, int index) {
		return "%s[%d]".formatted(node.getPath(), index);
	}
	private static int indexOfIdentity(List<Object> list, Object element) {
		for (var i = 0; i < list.size(); i++) if (list.get(i) == element) return i;
		return -1;
	}
	private static boolean isReadOnly(RowContext ctx, ValueConfigNode node) {
		if (!ctx.editable()) return true;
		var pending = ClientLockManager.getPendingLock(ctx.fileKey(), node.getLockPath());
		return pending != null ? pending : ClientLockManager.isLocked(ctx.fileKey(), node.getLockPath());
	}
	private static ValueConfigNode elementNode(ValueConfigNode node, RowContext ctx, int index, Class<?> elementType) {
		var list = values(node, ctx);
		var baseline = index < list.size() ? FieldAccess.deepCopy(list.get(index)) : null;
		return ValueConfigNode.builder()
			.valueType(FieldAccess.boxed(elementType))
			.name("%s[%d]".formatted(node.getName(), index))
			.path(elementPath(node, index))
			.title(node.getTitle().copy().append(" #" + (index + 1)))
			.defaultValue(defaultElement(node, index))
			.valueReader(ignored -> index < list.size() ? list.get(index) : null)
			.valueWriter((ignored, value) -> {
				if (index < list.size()) list.set(index, value);
			})
			.writeThrough(true)
			.element(true)
			.lockPath(node.getLockPath())
			.baselineValue(baseline)
			.build();
	}
	private static ValueConfigNode nestedElementNode(ValueConfigNode node, RowContext ctx, int index, ElementType shape) {
		var list = values(node, ctx);
		var raw = shape.raw();
		var array = raw.isArray();
		Object defaultValue = defaultElement(node, index);
		if (defaultValue != null && defaultValue.getClass().isArray()) defaultValue = FieldAccess.arrayToList(defaultValue);
		ValueReader reader;
		ValueWriter writer;
		Object baseline;
		if (array) {
			var componentType = raw.getComponentType();
			var holder = new WriteBackList[1];
			var cached = new WriteBackList(
				FieldAccess.arrayToList(list.get(index)), () -> {
				if (index < list.size()) list.set(index, FieldAccess.listToArray(componentType, holder[0]));
			}
			);
			holder[0] = cached;
			reader = ignored -> cached;
			writer = (ignored, value) -> cached.replaceContents(value instanceof List<?> values ? values : List.of());
			baseline = FieldAccess.deepCopy(cached);
		} else {
			reader = ignored -> index < list.size() ? list.get(index) : null;
			writer = (ignored, value) -> {
				if (index < list.size()) list.set(index, value);
			};
			baseline = FieldAccess.deepCopy(list.get(index));
		}
		if (defaultValue == null) defaultValue = new ArrayList<>();
		return ValueConfigNode.builder()
			.valueType(List.class)
			.name("%s[%d]".formatted(node.getName(), index))
			.path(elementPath(node, index))
			.title(node.getTitle().copy().append(" #" + (index + 1)))
			.defaultValue(defaultValue)
			.elementType(shape.element())
			.writeThrough(true)
			.element(true)
			.lockPath(node.getLockPath())
			.baselineValue(baseline)
			.valueReader(reader)
			.valueWriter(writer)
			.build();
	}
	private static void appendElements(RowContext ctx, ValueConfigNode node, int depth, List<ConfigRow> out) {
		var shape = node.getElementType();
		if (shape == null) return;
		var list = values(node, ctx);
		for (var i = 0; i < list.size(); i++) {
			var position = i;
			if (shape.isCollection()) {
				var start = out.size();
				append(ctx, nestedElementNode(node, ctx, i, shape), depth, out);
				if (out.size() > start) addDelete(out.get(start), ctx, node, position);
				continue;
			}
			if (shape.isSimple()) {
				var row = RowTypeRegistry.create(ctx, elementNode(node, ctx, i, shape.raw()));
				row.setDepth(depth);
				addDelete(row, ctx, node, position);
				out.add(row);
				continue;
			}
			var element = list.get(i);
			var key = "%s.%s".formatted(optionKey(ctx, node), element.getClass().getSimpleName());
			var path = elementPath(node, i);
			GroupRows.appendGroup(
				ctx,
				element,
				defaultElement(node, i),
				ConfigTranslation.translatable(key),
				path,
				key,
				node.getLockPath(),
				depth,
				() -> remove(ctx, node, position),
				!isReadOnly(ctx, node),
				out
			);
		}
	}
	private static String optionKey(RowContext ctx, ValueConfigNode node) {
		return "%s.config.option.%s".formatted(ctx.shell().root.modId(), node.getPath());
	}
	private static void addDelete(ConfigRow row, RowContext ctx, ValueConfigNode node, int index) {
		if (!(row instanceof ValueRow valueRow)) return;
		var delete = new UiButton(Component.literal("-"), Style.DANGER, () -> remove(ctx, node, index));
		delete.setEnabled(!isReadOnly(ctx, node));
		valueRow.addAction(delete, UiTheme.ACTION_SIZE);
	}
}
