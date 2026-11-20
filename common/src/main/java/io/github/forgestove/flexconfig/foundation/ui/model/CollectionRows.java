package io.github.forgestove.flexconfig.foundation.ui.model;
import io.github.forgestove.flexconfig.api.ConfigTranslationKeys;
import io.github.forgestove.flexconfig.foundation.*;
import io.github.forgestove.flexconfig.foundation.client.*;
import io.github.forgestove.flexconfig.foundation.tree.ValueConfigNode;
import io.github.forgestove.flexconfig.foundation.tree.ValueConfigNode.*;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.row.*;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiButton;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiButton.Style;
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
	/** 默认列表里存在同索引的元素才算有默认值；超出的元素是新增的，重置值只是兜底。 */
	private static boolean hasDefaultElement(ValueConfigNode node, int index) {
		return node.getDefaultValue() instanceof List<?> list && index < list.size();
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
		ctx.exitRow(
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
			.hasDefault(hasDefaultElement(node, index))
			// 撤销/重置会用新的列表替换编辑值，读写必须每次取当前列表，不能闭包旧的引用
			.valueReader(ignored -> {
				var current = values(node, ctx);
				return index < current.size() ? current.get(index) : null;
			})
			.valueWriter((ignored, value) -> {
				var current = values(node, ctx);
				if (index < current.size()) current.set(index, value);
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
			var source = new Object[1];
			reader = ignored -> {
				var current = values(node, ctx);
				var element = index < current.size() ? current.get(index) : null;
				// 列表被整体替换后（撤销/重置），数组元素也要重新绑定一次
				if (element != source[0]) {
					source[0] = element;
					holder[0] = new WriteBackList(
						element == null ? new ArrayList<>() : FieldAccess.arrayToList(element), () -> {
						var now = values(node, ctx);
						if (index < now.size()) now.set(index, FieldAccess.listToArray(componentType, holder[0]));
					}
					);
				}
				return holder[0];
			};
			writer = (ignored, value) -> ((WriteBackList) reader.read(null)).replaceContents(value instanceof List<?> values
				? values
				: List.of());
			baseline = FieldAccess.deepCopy(reader.read(null));
		} else {
			reader = ignored -> {
				var current = values(node, ctx);
				return index < current.size() ? current.get(index) : null;
			};
			writer = (ignored, value) -> {
				var current = values(node, ctx);
				if (index < current.size()) current.set(index, value);
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
			.hasDefault(hasDefaultElement(node, index))
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
				Translation.from(ConfigTranslation.translatable(key)),
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
		return ConfigTranslationKeys.option(ctx.modId(), node.getPath());
	}
	private static void addDelete(ConfigRow row, RowContext ctx, ValueConfigNode node, int index) {
		if (!(row instanceof ValueRow valueRow)) return;
		var delete = new UiButton(UiText.literal("-"), Style.DANGER, () -> remove(ctx, node, index));
		delete.setEnabled(!isReadOnly(ctx, node));
		valueRow.addAction(delete, UiTheme.ACTION_SIZE);
	}
}
