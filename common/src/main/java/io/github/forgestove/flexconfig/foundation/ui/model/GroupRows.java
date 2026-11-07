package io.github.forgestove.flexconfig.foundation.ui.model;
import io.github.forgestove.flexconfig.api.annotation.*;
import io.github.forgestove.flexconfig.api.registration.ConfigCondition;
import io.github.forgestove.flexconfig.foundation.*;
import io.github.forgestove.flexconfig.foundation.tree.ValueConfigNode;
import io.github.forgestove.flexconfig.foundation.tree.ValueConfigNode.*;
import io.github.forgestove.flexconfig.foundation.ui.row.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.List;
/** 对象元素分组。 */
public class GroupRows {
	public static void appendGroup(
		RowContext ctx,
		Object target,
		@Nullable Object defaults,
		Component label,
		String path,
		String keyPrefix,
		String lockPath,
		int depth,
		@Nullable Runnable onDelete,
		boolean canDelete,
		List<ConfigRow> out
	) {
		out.add(new GroupHeaderRow(ctx, target, path, label, depth, canDelete, onDelete));
		appendChildren(ctx, target, defaults, path, keyPrefix, lockPath, depth + 1, out);
	}
	public static void appendChildren(
		RowContext ctx,
		Object target,
		@Nullable Object defaults,
		String path,
		String keyPrefix,
		String lockPath,
		int depth,
		List<ConfigRow> out
	) {
		for (var field : FieldAccess.collectOrderedFields(target.getClass())) {
			if (!ConfigCondition.evaluate(field)) continue;
			if (field.isAnnotationPresent(Category.class)) {
				var childTarget = FieldAccess.getFieldValue(field, target);
				if (childTarget == null) {
					childTarget = FieldAccess.newInstance(field.getType());
					if (childTarget == null) continue;
					FieldAccess.setFieldValue(field, target, childTarget);
				}
				var childDefaults = defaults == null ? null : FieldAccess.getFieldValue(field, defaults);
				var childKey = "%s.%s".formatted(keyPrefix, field.getName());
				appendGroup(
					ctx,
					childTarget,
					childDefaults,
					ConfigTranslation.translatable(childKey),
					"%s.%s".formatted(path, field.getName()),
					childKey,
					lockPath,
					depth,
					null,
					false,
					out
				);
				continue;
			}
			var node = fieldNode(field, target, defaults, path, keyPrefix, lockPath);
			if (node.getValueType() == List.class) CollectionRows.append(ctx, node, depth, out);
			else {
				var row = RowTypeRegistry.create(ctx, node);
				row.setDepth(depth);
				out.add(row);
			}
		}
	}
	private static ValueConfigNode fieldNode(
		Field field,
		Object target,
		@Nullable Object defaults,
		String path,
		String keyPrefix,
		String lockPath
	) {
		var rawType = field.getType();
		var isArray = rawType.isArray();
		var valueType = isArray ? List.class : FieldAccess.boxed(rawType);
		var elementType = FieldAccess.elementShape(field);
		Object defaultValue = defaults == null ? null : FieldAccess.getFieldValue(field, defaults);
		if (isArray) defaultValue = FieldAccess.arrayToList(defaultValue);
		Object baseline = FieldAccess.getFieldValue(field, target);
		if (isArray) baseline = FieldAccess.arrayToList(baseline);
		var fieldKey = "%s.%s".formatted(keyPrefix, field.getName());
		ValueReader reader;
		ValueWriter writer;
		if (isArray) {
			var componentType = rawType.getComponentType();
			var holder = new WriteBackList[1];
			var cached = new WriteBackList(
				FieldAccess.arrayToList(FieldAccess.getFieldValue(field, target)),
				() -> FieldAccess.setFieldValue(field, target, FieldAccess.listToArray(componentType, holder[0]))
			);
			holder[0] = cached;
			reader = ignored -> cached;
			writer = (ignored, value) -> cached.replaceContents(value instanceof List<?> values ? values : List.of());
		} else {
			reader = ignored -> FieldAccess.getFieldValue(field, target);
			writer = (ignored, value) -> FieldAccess.setFieldValue(field, target, value);
		}
		return ValueConfigNode.builder()
			.valueType(valueType)
			.field(field)
			.name(field.getName())
			.path("%s.%s".formatted(path, field.getName()))
			.title(ConfigTranslation.translatable(fieldKey))
			.tooltip(ConfigTranslation.translatable(fieldKey + ".tooltip"))
			.defaultValue(defaultValue)
			.elementType(elementType)
			.colorValue(field.getAnnotation(ColorValue.class))
			.sliderRange(FieldValidators.sliderRange(field))
			.validator(FieldValidators.validatorFor(valueType, field))
			.writeThrough(true)
			.element(true)
			.lockPath(lockPath)
			.baselineValue(FieldAccess.deepCopy(baseline))
			.valueReader(reader)
			.valueWriter(writer)
			.build();
	}
}
