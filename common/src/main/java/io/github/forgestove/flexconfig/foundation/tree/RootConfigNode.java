package io.github.forgestove.flexconfig.foundation.tree;
import com.google.common.collect.ImmutableList;
import io.github.forgestove.flexconfig.api.*;
import io.github.forgestove.flexconfig.api.annotation.*;
import io.github.forgestove.flexconfig.api.registration.ConfigCondition;
import io.github.forgestove.flexconfig.foundation.*;
import io.github.forgestove.flexconfig.foundation.client.Translation;
import io.github.forgestove.flexconfig.foundation.tree.ValueConfigNode.*;
import io.github.forgestove.flexconfig.platform.FlexConfigServices;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;

import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.util.*;
public record RootConfigNode(
	String modId, String fileName, ImmutableList<CategoryConfigNode> categories, ImmutableList<String> translationKeys
) implements ConfigNode {
	public static @NotNull RootConfigNode create(Object defaultConfig, String modId, String fileName, ConfigType type) {
		return new Builder(defaultConfig, modId, fileName, type).build();
	}
	public @NotNull Component getTitle() {
		return Component.empty();
	}
	@Override
	public @Nullable Component getTooltip() {
		return null;
	}
	@Override
	public void resetToDefault() {
		categories.forEach(ConfigNode::resetToDefault);
	}
	@Override
	public void resetToActive(Object config) {
		categories.forEach(category -> category.resetToActive(config));
	}
	@Nullable
	@Override
	public RestartType restartType(Object config) {
		RestartType worst = null;
		for (var category : categories) {
			var type = category.restartType(config);
			if (type == RestartType.GAME) return RestartType.GAME;   // 取最重的一档
			if (type == RestartType.WORLD) worst = RestartType.WORLD;
		}
		return worst;
	}
	@Override
	public boolean isDefaultValue(Object config) {
		return categories.stream().allMatch(node -> node.isDefaultValue(config));
	}
	@Override
	public boolean isActiveValue(Object config) {
		return categories.stream().allMatch(node -> node.isActiveValue(config));
	}
	@Override
	public @Nullable Component validate(Object config) {
		Component error = null;
		for (var node : categories) {
			var result = node.validate(config);
			if (result == null) continue;
			if (error == null) error = result;
			else return Translation.MULTIPLE_ERRORS;
		}
		return error;
	}
	@Override
	public void copy(Object from, Object to) {
		categories.forEach(node -> node.copy(from, to));
	}
	@Override
	public void copyForRuntime(Object from, Object to) {
		categories.forEach(node -> node.copyForRuntime(from, to));
	}
	@Override
	public void flushPending(Object to) {
		categories.forEach(node -> node.flushPending(to));
	}
	@Override
	public void applyRestart(Object saved, Object active, RestartType restartType) {
		ConfigChangeDispatcher.begin();
		try {
			categories.forEach(node -> node.applyRestart(saved, active, restartType));
		} finally {
			ConfigChangeDispatcher.end();   // finally：节点抛异常也不会让派发深度卡住
		}
	}
	@Override
	public void writeEditingToConfig(Object config) {
		ConfigChangeDispatcher.begin();
		try {
			categories.forEach(node -> node.writeEditingToConfig(config));
		} finally {
			ConfigChangeDispatcher.end();   // finally：节点抛异常也不会让派发深度卡住
		}
	}
	@Override
	@NotNull
	public ImmutableList<CategoryConfigNode> categories() {
		return categories;
	}
	@Nullable
	public ValueConfigNode getValueNode(String path) {
		for (var category : categories) {
			var result = findValueNode(category, path);
			if (result != null) return result;
		}
		return null;
	}
	@Nullable
	private ValueConfigNode findValueNode(ConfigNode node, String path) {
		if (node instanceof ValueConfigNode valueNode && valueNode.getPath().equals(path)) return valueNode;
		if (node instanceof CategoryConfigNode categoryNode) for (var child : categoryNode.getChildren()) {
			var result = findValueNode(child, path);
			if (result != null) return result;
		}
		return null;
	}
	private static class Builder {
		private final String modId;
		private final String fileName;
		private final ConfigType type;
		/** 构建期收集的翻译键，供开发期检查缺失。 */
		private final ImmutableList.Builder<String> translationKeys = ImmutableList.builder();
		private Object defaultConfig;
		private Builder(Object defaultConfig, String modId, String fileName, ConfigType type) {
			this.defaultConfig = defaultConfig;
			this.modId = modId;
			this.fileName = fileName;
			this.type = type;
		}
		private static boolean evaluatePathCondition(@NotNull List<Field> path) {
			for (var field : path)
				if (!ConfigCondition.evaluate(field)) return false;
			return true;
		}
		@NotNull
		public RootConfigNode build() {
			var categories = Arrays.stream(FieldAccess.collectOrderedFields(defaultConfig.getClass()))
				.filter(field -> field.isAnnotationPresent(Category.class))
				.filter(ConfigCondition::evaluate)
				.map(this::createCategoryNode)
				.collect(ImmutableList.toImmutableList());
			defaultConfig = null;
			return new RootConfigNode(modId, fileName, categories, translationKeys.build());
		}
		private CategoryConfigNode createCategoryNode(Field categoryField) {
			var defaultCategory = FieldAccess.getFieldValue(categoryField, defaultConfig);
			var categoryKey = modId + ".config.category." + categoryField.getName();
			translationKeys.add(categoryKey);
			var categoryBuilder = CategoryConfigNode.builder().title(ConfigTranslation.translatable(categoryKey));
			for (var valueField : FieldAccess.collectOrderedFields(categoryField.getType()))
				if (valueField.isAnnotationPresent(Category.class)) {
					if (!ConfigCondition.evaluate(valueField)) continue;
					var subBuilder = createSubCategoryNode(List.of(categoryField), valueField);
					categoryBuilder.category(b -> subBuilder);
				} else addValueNode(defaultCategory, List.of(categoryField), valueField, categoryBuilder);
			return categoryBuilder.build();
		}
		private CategoryConfigNode.Builder createSubCategoryNode(List<Field> parentPath, Field subCategoryField) {
			var path = new ArrayList<>(parentPath);
			path.add(subCategoryField);
			Object defaultParent = defaultConfig;
			for (var field : parentPath) defaultParent = FieldAccess.getFieldValue(field, defaultParent);
			var defaultSubCategory = FieldAccess.getFieldValue(subCategoryField, defaultParent);
			var pathKey = buildPathKey(path);
			var categoryKey = modId + ".config.category." + pathKey;
			translationKeys.add(categoryKey);
			var builder = CategoryConfigNode.builder().title(ConfigTranslation.translatable(categoryKey));
			for (var valueField : FieldAccess.collectOrderedFields(subCategoryField.getType()))
				if (valueField.isAnnotationPresent(Category.class)) {
					if (!ConfigCondition.evaluate(valueField)) continue;
					if (!evaluatePathCondition(path)) continue;
					var subBuilder = createSubCategoryNode(path, valueField);
					builder.category(b -> subBuilder);
				} else addValueNode(defaultSubCategory, path, valueField, builder);
			return builder;
		}
		private @NotNull String buildPathKey(@NotNull List<Field> fields) {
			if (fields.isEmpty()) return "";
			var sb = new StringBuilder();
			for (var f : fields) {
				if (!sb.isEmpty()) sb.append('.');
				sb.append(f.getName());
			}
			return sb.toString();
		}
		private void addValueNode(Object defaultCategory, List<Field> path, Field valueField, CategoryConfigNode.Builder categoryBuilder) {
			if (!ConfigCondition.evaluate(valueField)) return;
			if (!evaluatePathCondition(path)) return;
			var rawType = valueField.getType();
			var defaultValue = FieldAccess.getFieldValue(valueField, defaultCategory);
			// 数组在配置树里按 List 编辑，写入文件时再转回数组
			var type = rawType.isArray() ? List.class : FieldAccess.boxed(rawType);
			var treeDefault = rawType.isArray() ? FieldAccess.arrayToList(defaultValue) : defaultValue;
			var elementType = FieldAccess.elementShape(valueField);
			var valueName = valueField.getName();
			var pathKey = buildPathKey(path);
			var titleKey = "%s.config.option.%s.%s".formatted(modId, pathKey, valueName);
			translationKeys.add(titleKey);
			translationKeys.add(titleKey + ".tooltip");
			// 枚举值的显示键：<modId>.config.enum.<枚举类名>.<常量名>
			if (type.isEnum()) for (var constant : type.getEnumConstants())
				translationKeys.add("%s.config.enum.%s.%s".formatted(modId, type.getSimpleName(), ((Enum<?>) constant).name()));
			else if (elementType != null && elementType.raw().isEnum()) for (var constant : elementType.raw().getEnumConstants())
				translationKeys.add("%s.config.enum.%s.%s".formatted(
					modId,
					elementType.raw().getSimpleName(),
					((Enum<?>) constant).name()
				));
			else if (elementType != null && !elementType.isSimple()) collectElementTranslationKeys(elementType, titleKey);
			var fullPath = (pathKey.isEmpty() ? "" : pathKey + ".") + valueName;
			var tooltip = ConfigTranslation.translatable(titleKey + ".tooltip");
			if (valueField.isAnnotationPresent(WarnCheat.class)) tooltip = tooltip.copy()
				.append(Component.literal("\n"))
				.append(Translation.WARN_CHEAT_TOOLTIP.copy().withStyle(ChatFormatting.RED));
			final var finalTooltip = tooltip;
			categoryBuilder.value(valueBuilder -> valueBuilder.valueType(type)
				.field(valueField)
				.name(valueName)
				.path(fullPath)
				.title(ConfigTranslation.translatable(titleKey))
				.tooltip(finalTooltip)
				.defaultValue(treeDefault)
				.colorValue(valueField.getAnnotation(ColorValue.class))
				.elementType(elementType)
				.valueReader(makePathValueReader(type, path, valueField))
				.valueWriter(makePathValueWriter(type, path, valueField))
				.restartType(restartTypeOf(valueField))
				.changeCallback(makeChangeCallback(valueField, fullPath))
				.sliderRange(FieldValidators.sliderRange(valueField))
				.validator(FieldValidators.validatorFor(type, valueField)));
		}
		/** 检查当前配置类型是否支持声明的重启级别，不支持就在构建配置树时抛异常。 */
		private @Nullable RestartType restartTypeOf(Field field) {
			var annotation = field.getAnnotation(RequiresRestart.class);
			if (annotation == null) return null;
			var restartType = annotation.value();
			if (!restartType.isValid(type)) throw new IllegalArgumentException(
				"Option '%s' of %s config %s declares restart type %s, which cannot be used for that config type".formatted(
					field.getName(),
					type,
					fileName,
					restartType
				));
			return restartType;
		}
		private @Nullable ConfigChangeCallback makeChangeCallback(Field valueField, String path) {
			if (!FlexConfigServices.PLATFORM.isClient()) return null;
			var annotation = valueField.getAnnotation(OnChange.class);
			if (annotation == null) return null;
			var type = annotation.value();
			return (oldValue, newValue) -> ConfigChangeDispatcher.record(type, path, oldValue, newValue);
		}
		private @NotNull ValueReader makePathValueReader(Class<?> type, @NotNull List<Field> path, Field valueField) {
			var pathHandles = path.stream().map(FieldAccess::varHandle).toArray(VarHandle[]::new);
			var valueHandle = FieldAccess.varHandle(valueField);
			var isArray = valueField.getType().isArray();
			return config -> {
				Object current = config;
				for (var handle : pathHandles) current = handle.get(current);
				Object value = FieldAccess.readField(valueHandle, current, valueField);
				if (isArray) value = FieldAccess.arrayToList(value);
				return type.cast(value);
			};
		}
		private @NotNull ValueWriter makePathValueWriter(Class<?> type, @NotNull List<Field> path, Field valueField) {
			var pathHandles = path.stream().map(FieldAccess::varHandle).toArray(VarHandle[]::new);
			var valueHandle = FieldAccess.varHandle(valueField);
			var elementType = valueField.getType().isArray() ? valueField.getType().getComponentType() : null;
			return (config, value) -> {
				Object current = config;
				for (var handle : pathHandles) current = handle.get(current);
				var target = elementType != null
					? value == null ? null : FieldAccess.listToArray(elementType, (List<?>) value)
					: type.cast(value);
				FieldAccess.writeField(valueHandle, current, target, valueField);
			};
		}
		/** 集合元素对象的字段键：{@code <列表选项键>.<元素类名>.<字段名>}；嵌套集合取最内层元素类型。 */
		private void collectElementTranslationKeys(ElementType shape, String listKey) {
			if (shape.isCollection()) {
				if (shape.element() != null) collectElementTranslationKeys(shape.element(), listKey);
				return;
			}
			if (shape.isSimple()) return;
			collectObjectTranslationKeys(shape.raw(), "%s.%s".formatted(listKey, shape.raw().getSimpleName()));
		}
		/** 对象元素及其 {@code @Category} 子对象的显示键。 */
		private void collectObjectTranslationKeys(Class<?> type, String prefix) {
			translationKeys.add(prefix);
			for (var field : FieldAccess.collectOrderedFields(type)) {
				var fieldKey = "%s.%s".formatted(prefix, field.getName());
				translationKeys.add(fieldKey);
				translationKeys.add(fieldKey + ".tooltip");
				if (field.isAnnotationPresent(Category.class)) {
					collectObjectTranslationKeys(field.getType(), fieldKey);
					continue;
				}
				var fieldType = field.getType().isArray() ? field.getType().getComponentType() : field.getType();
				if (!fieldType.isEnum()) continue;
				for (var constant : fieldType.getEnumConstants())
					translationKeys.add("%s.config.enum.%s.%s".formatted(modId, fieldType.getSimpleName(), ((Enum<?>) constant).name()));
			}
		}
	}
}
