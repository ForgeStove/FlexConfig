package io.github.forgestove.flexconfig.foundation;
import com.electronwill.nightconfig.core.*;
import com.electronwill.nightconfig.core.io.*;
import com.electronwill.nightconfig.toml.*;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.api.annotation.*;
import io.github.forgestove.flexconfig.api.registration.*;
import io.github.forgestove.flexconfig.foundation.client.Translation;
import io.github.forgestove.flexconfig.platform.FlexConfigServices;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.awt.Point;
import java.lang.reflect.Field;
import java.nio.file.*;
import java.util.*;
public class ConfigSerializer<C> {
	private static final String KEYBINDS_SECTION = "-keybinds-";
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Class<C> configClass;
	private final String translationPrefix;
	/** 由 {@link ConfigHandler} 解析路径后注入。 */
	private Path targetPath;
	private ConfigSerializer(Builder<C> builder) {
		configClass = builder.configClass;
		translationPrefix = ConfigStore.getModId(configClass) + ".config";
	}
	public static <C> Builder<C> builder(Class<C> configClass) {
		return new Builder<>(configClass);
	}
	/** 不支持或非法的值返回 {@code null}，由调用方跳过该条。 */
	private static @Nullable Object toTomlValue(Field field, @Nullable Object value) {
		if (value == null) return warnNull(field, "value is null");
		if (field.isAnnotationPresent(ColorValue.class) && !(value instanceof Integer))
			return warnNull(field, "color value is not an integer");
		return toTomlElement(field, value);
	}
	/** 标量、点、集合与普通对象统一转换；任意一层失败则整条放弃。 */
	@Nullable
	private static Object toTomlElement(Field field, @Nullable Object value) {
		if (value == null) return warnNull(field, "value is null");
		if (value instanceof Point point) return pointConfig(point);
		if (value.getClass().isArray()) return toTomlList(field, FieldAccess.arrayToList(value));
		if (value instanceof List<?> list) return toTomlList(field, list);
		if (FieldAccess.isSimpleType(value.getClass())) return scalar(field, value);
		if (!isConfigObject(value.getClass())) return warnNull(field, "unsupported type " + value.getClass().getName());
		return objectConfig(value);
	}
	/** 只有用户自己的普通对象才写成子表；引擎与 JDK 类型保持原有的「跳过」行为。 */
	private static boolean isConfigObject(Class<?> type) {
		var name = type.getName();
		return !name.startsWith("java.") && !name.startsWith("javax.") && !name.startsWith("com.mojang.") && !name.startsWith(
			"net.minecraft.");
	}
	/** 出现不支持的列表元素时整条放弃，返回 {@code null}。 */
	@Nullable
	private static List<Object> toTomlList(Field field, List<?> list) {
		var result = new ArrayList<>(list.size());
		for (var element : list) {
			if (element == null) {
				warnNull(field, "list contains a null element");
				return null;
			}
			var converted = toTomlElement(field, element);
			if (converted == null) return null;
			result.add(converted);
		}
		return result;
	}
	/** 普通对象写成子表；{@code @Category} 字段同样递归成子表。 */
	private static CommentedConfig objectConfig(Object value) {
		var config = inMemory();
		for (var subField : FieldAccess.collectOrderedFields(value.getClass())) {
			var subValue = toTomlElement(subField, FieldAccess.getFieldValue(subField, value));
			if (subValue != null) config.set(subField.getName(), subValue);
		}
		return config;
	}
	@Nullable
	private static Object scalar(Field field, Object value) {
		return switch (value) {
			case Boolean bool -> bool;
			case Integer intValue -> intValue;
			case Long longValue -> longValue;
			case Short shortValue -> shortValue;
			case Byte byteValue -> byteValue;
			case Double doubleValue -> finite(field, doubleValue);
			case Float floatValue -> finite(field, floatValue);
			case Enum<?> anEnum -> anEnum.name();
			case String string -> string;
			case Character character -> String.valueOf(character);
			case null, default -> warnNull(field, "unsupported type " + Objects.requireNonNull(value).getClass().getName());
		};
	}
	/** NaN 与 Infinity 不是合法的 TOML 浮点，跳过并告警。 */
	@Nullable
	private static Double finite(Field field, double value) {
		if (Double.isNaN(value) || Double.isInfinite(value)) {
			warnNull(field, "value is not a finite number");
			return null;
		}
		return value;
	}
	@Nullable
	private static Float finite(Field field, float value) {
		if (Float.isNaN(value) || Float.isInfinite(value)) {
			warnNull(field, "value is not a finite number");
			return null;
		}
		return value;
	}
	/** 点写成子表 {@code {x, y}}，读侧按同样的子表结构取值。 */
	private static CommentedConfig pointConfig(Point point) {
		var config = inMemory();
		config.set("x", point.x);
		config.set("y", point.y);
		return config;
	}
	@Nullable
	private static Object warnNull(Field field, String reason) {
		LOGGER.warn("Skipping option '{}': {}", field.getName(), reason);
		return null;
	}
	/** 含长度校正；字段类型不符或字符串过短无法修正时返回 {@code false}。 */
	private static boolean setString(Object category, Field field, String value) throws IllegalAccessException {
		if (field.getType() != String.class) return false;
		var corrected = FieldValidators.correct(field, value);
		if (corrected == null) return false;
		field.set(category, corrected);
		return true;
	}
	/** 出现不支持的元素时整条放弃，返回 {@code null}。 */
	@Nullable
	private static List<Object> toList(Field field, @Nullable ElementType shape, List<?> raw) {
		if (shape == null) {
			LOGGER.warn("Skipping option '{}': raw List without an element type is not supported", field.getName());
			return null;
		}
		var result = new ArrayList<>(raw.size());
		for (var element : raw) {
			var converted = toElement(field, shape, element);
			if (converted == null) return null;
			result.add(converted);
		}
		return result;
	}
	@Nullable
	private static Object toElement(Field field, ElementType shape, @Nullable Object element) {
		if (element == null) return warnNull(field, "list contains a null element");
		if (shape.isList()) {
			if (!(element instanceof List<?> nested)) return warnNull(field, "nested list element is not a list");
			return toList(field, shape.element(), nested);
		}
		if (shape.isArray()) {
			if (!(element instanceof List<?> nested)) return warnNull(field, "nested array element is not a list");
			var list = toList(field, shape.element(), nested);
			return list == null ? null : FieldAccess.listToArray(shape.raw().getComponentType(), list);
		}
		var elementType = shape.raw();
		if (elementType.isInstance(element)) return element;
		if (element instanceof String name) {
			var constant = enumValue(elementType, name);
			if (constant != null) return constant;
		}
		switch (element) {
			case CommentedConfig pointConfig when elementType == Point.class -> {
				return new Point(pointConfig.getInt("x"), pointConfig.getInt("y"));
			}
			case String string when elementType == Character.class || elementType == char.class -> {
				return string.length() == 1 ? string.charAt(0) : warnNull(field, "character element is not a single character");
			}
			case CommentedConfig config when !FieldAccess.isSimpleType(elementType) && isConfigObject(elementType) -> {
				return readObject(field, elementType, config);
			}
			case Number num -> {
				if (elementType == Integer.class || elementType == int.class) return num.intValue();
				if (elementType == Long.class || elementType == long.class) return num.longValue();
				if (elementType == Double.class || elementType == double.class) return num.doubleValue();
				if (elementType == Float.class || elementType == float.class) return num.floatValue();
				if (elementType == Short.class || elementType == short.class) return num.shortValue();
				if (elementType == Byte.class || elementType == byte.class) return num.byteValue();
				if (elementType == String.class) return num.toString();
				return warnNull(field, "unsupported list element type " + elementType.getSimpleName());
			}
			default -> {
			}
		}
		return warnNull(field, "list element does not match " + elementType.getSimpleName());
	}
	/** 不是枚举类型时返回 {@code null}，由调用方按普通值继续处理。 */
	@Nullable
	private static Object enumValue(Class<?> type, String name) {
		var constants = type.getEnumConstants();
		if (constants == null) return null;
		for (var constant : constants) if (((Enum<?>) constant).name().equals(name)) return constant;
		throw new IllegalArgumentException("No enum constant %s.%s".formatted(type.getCanonicalName(), name));
	}
	/** 列表元素是普通对象时按字段名反序列化。 */
	@Nullable
	private static Object readObject(Field field, Class<?> type, CommentedConfig config) {
		var instance = FieldAccess.newInstance(type);
		if (instance == null) return warnNull(field, "cannot instantiate nested object type " + type.getSimpleName());
		for (var subField : FieldAccess.collectOrderedFields(type)) {
			var raw = config.get(subField.getName());
			if (raw == null) continue;
			try {
				if (!applyValue(instance, subField, raw)) LOGGER.warn(
					"Ignoring nested field '{}': value '{}' does not match type {}",
					subField.getName(),
					raw,
					subField.getType().getSimpleName()
				);
			} catch (Exception e) {
				LOGGER.warn("Ignoring option '{}': failed to read nested value", subField.getName(), e);
			}
		}
		return instance;
	}
	/** 快捷键段：键是配置路径（含点号），必须按字面键写入，否则点号会被解析成层级。 */
	private static void writeKeybinds(CommentedConfig toml, Map<String, String> keybinds) {
		if (keybinds.isEmpty()) return;
		var section = toml.createSubConfig();
		keybinds.forEach((path, value) -> section.set(List.of(path), value));
		toml.set(KEYBINDS_SECTION, section);
	}
	/** 读取快捷键段；兼容旧的、点号被解析成层级的写法。 */
	public static Map<String, String> readKeybinds(@Nullable CommentedConfig toml) {
		var result = new LinkedHashMap<String, String>();
		Object raw = toml == null ? null : toml.get(KEYBINDS_SECTION);
		if (raw instanceof UnmodifiableConfig section) flatten(section, "", result);
		return result;
	}
	private static void flatten(UnmodifiableConfig config, String prefix, Map<String, String> out) {
		for (var entry : config.entrySet()) {
			var path = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
			var value = entry.getValue();
			if (value instanceof UnmodifiableConfig sub) flatten(sub, path, out);
			else if (value instanceof String string) out.put(path, string);
		}
	}
	public static @Nullable CommentedConfig read(Path path) throws SerializationException {
		if (!Files.exists(path)) return null;
		try (var reader = Files.newBufferedReader(path)) {
			var toml = inMemory();
			new TomlParser().parse(reader, toml, ParsingMode.REPLACE);
			return toml;
		} catch (Exception e) {
			throw new SerializationException(e);
		}
	}
	public static CommentedConfig inMemory() {
		return CommentedConfig.of(LinkedHashMap::new, InMemoryCommentedFormat.defaultInstance());
	}
	/** 含范围校正；类型不匹配或不受支持时返回 {@code false}，由调用方保留默认值。 */
	private static boolean applyValue(Object category, Field field, Object value) throws IllegalAccessException {
		field.setAccessible(true);
		var type = field.getType();
		if (type.isArray()) {
			if (!(value instanceof List<?> raw)) return false;
			var elementType = FieldAccess.elementShape(field);
			if (elementType == null) return false;
			var list = toList(field, elementType, raw);
			if (list == null) return false;
			field.set(category, FieldAccess.listToArray(type.getComponentType(), list));
			return true;
		}
		if (type == Point.class) {
			if (!(value instanceof CommentedConfig cc)) return false;
			field.set(category, new Point(cc.getInt("x"), cc.getInt("y")));
			return true;
		}
		if (value instanceof String name) {
			var constant = enumValue(type, name);
			if (constant != null) {
				field.set(category, constant);
				return true;
			}
		}
		if (value instanceof Number num) {
			if (type == int.class || type == Integer.class) field.setInt(category, FieldValidators.correct(field, num.intValue()));
			else if (type == long.class || type == Long.class) field.setLong(category, FieldValidators.correct(field, num.longValue()));
			else if (type == double.class || type == Double.class)
				field.setDouble(category, FieldValidators.correct(field, num.doubleValue()));
			else if (type == float.class || type == Float.class) field.setFloat(category, FieldValidators.correct(field,
				num.floatValue()));
			else if (type == byte.class || type == Byte.class) field.setByte(category, num.byteValue());
			else if (type == short.class || type == Short.class) field.setShort(category, num.shortValue());
			else return setString(category, field, num.toString());
			return true;
		}
		if (List.class.isAssignableFrom(type)) {
			if (!(value instanceof List<?> raw)) return false;
			var elementType = FieldAccess.elementShape(field);
			if (elementType == null) {
				LOGGER.warn("Option '{}' uses a raw List; declare a generic element type", field.getName());
				return false;
			}
			var list = toList(field, elementType, raw);
			if (list == null) return false;
			field.set(category, list);
			return true;
		}
		if (value instanceof CommentedConfig config && !FieldAccess.isSimpleType(type) && isConfigObject(type)) {
			var object = readObject(field, type, config);
			if (object == null) return false;
			field.set(category, object);
			return true;
		}
		// 原始 boolean 的 isInstance 恒为 false，要单独处理
		if (value instanceof Boolean bool && (type == boolean.class || type == Boolean.class)) {
			field.set(category, bool);
			return true;
		}
		if (value instanceof String string && (type == char.class || type == Character.class)) {
			if (string.length() != 1) return false;
			field.set(category, string.charAt(0));
			return true;
		}
		if (value instanceof String string) return setString(category, field, string);
		if (!type.isInstance(value)) return false;
		field.set(category, value);
		return true;
	}
	public void setTargetPath(Path targetPath) {
		this.targetPath = targetPath;
	}
	/** 先写临时文件再原子替换，写入失败时原文件保持完整。 */
	public void serialize(C config, Map<String, String> keybinds) throws SerializationException {
		try {
			var toml = toConfig(config);
			writeKeybinds(toml, keybinds);
			new TomlWriter().write(toml, targetPath(), WritingMode.REPLACE_ATOMIC);
		} catch (Exception e) {
			throw new SerializationException(e);
		}
	}
	/** 不含快捷键段，网络下发时使用。 */
	public String writeToString(C config) throws SerializationException {
		try {
			return new TomlWriter().writeToString(toConfig(config));
		} catch (Exception e) {
			throw new SerializationException(e);
		}
	}
	/** 文件不存在时返回 {@code null}。 */
	public @Nullable CommentedConfig read() throws SerializationException {
		return read(targetPath());
	}
	private Path targetPath() {
		if (targetPath == null) throw new IllegalStateException("Config target path has not been resolved yet");
		return targetPath;
	}
	private CommentedConfig toConfig(C config) throws SerializationException {
		var toml = inMemory();
		try {
			for (var categoryField : getCategoryFields()) {
				categoryField.setAccessible(true);
				writeCategory(toml, categoryField.get(config), categoryField.getName());
			}
			return toml;
		} catch (Exception e) {
			throw new SerializationException(e);
		}
	}
	/** 从已解析的 {@link CommentedConfig} 反序列化，不经文件读取。 */
	public C deserializeFrom(CommentedConfig config) throws SerializationException {
		try {
			var instance = newInstance();
			var fallbackValues = buildFlatValueMap(config);
			for (var categoryField : getCategoryFields()) {
				categoryField.setAccessible(true);
				readCategory(config, categoryField.getName(), categoryField.get(instance), fallbackValues);
			}
			return instance;
		} catch (Exception e) {
			throw new SerializationException(e);
		}
	}
	private Field[] getCategoryFields() {
		List<Field> list = new ArrayList<>();
		for (var field : FieldAccess.collectOrderedFields(configClass))
			if (field.isAnnotationPresent(Category.class)) list.add(field);
		return list.toArray(new Field[0]);
	}
	private C newInstance() throws SerializationException {
		try {
			return configClass.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException e) {
			throw new SerializationException(e);
		}
	}
	private void writeCategory(CommentedConfig parent, Object category, String path) throws IllegalAccessException {
		var name = path.substring(path.lastIndexOf('.') + 1);
		var comment = translate("category." + path);
		if (comment != null) parent.setComment(List.of(name), " " + comment.trim());
		var section = parent.createSubConfig();
		parent.set(name, section);
		for (var field : FieldAccess.collectOrderedFields(category.getClass())) {
			field.setAccessible(true);
			var fieldName = field.getName();
			if (field.isAnnotationPresent(Category.class)) {
				writeCategory(section, field.get(category), "%s.%s".formatted(path, fieldName));
				continue;
			}
			// 先算值：不支持的值直接跳过，避免留下悬空注释
			var value = toTomlValue(field, field.get(category));
			if (value == null) continue;
			var fieldComment = buildFieldComment(path, fieldName);
			if (!fieldComment.isEmpty()) section.setComment(List.of(fieldName), fieldComment);
			section.set(fieldName, value);
		}
	}
	private String buildFieldComment(String categorySnake, String fieldName) {
		var optionKey = "option.%s.%s".formatted(categorySnake, fieldName);
		var title = translate(optionKey);
		var tooltip = translate(optionKey + ".tooltip");
		var sb = new StringBuilder();
		if (title != null) sb.append(" ").append(title);
		if (tooltip != null) for (var line : tooltip.split("\n")) {
			if (!sb.isEmpty()) sb.append("\n");
			sb.append(" ").append(line);
		}
		return sb.toString();
	}
	private String translate(String key) {
		// Translation 依赖客户端 I18n，专用服务端调用会加载客户端类；服务端不生成注释，不会进入这个方法
		if (translationPrefix == null || !FlexConfigServices.PLATFORM.isClient()) return null;
		key = "%s.%s".formatted(translationPrefix, key);
		return Translation.getString(key);
	}
	/** 字段名到值的平铺映射：字段被移到别的分类路径后，仍可按名称找回旧值。 */
	private Map<String, Object> buildFlatValueMap(CommentedConfig config) {
		var map = new HashMap<String, Object>();
		buildFlatValueMapRecursive(config, map);
		return map;
	}
	private void buildFlatValueMapRecursive(CommentedConfig config, Map<String, Object> map) {
		for (var entry : config.entrySet()) {
			var key = entry.getKey();
			var value = entry.getValue();
			if (value instanceof CommentedConfig sub) buildFlatValueMapRecursive(sub, map);
			else map.putIfAbsent(key, value);
		}
	}
	private void readCategory(CommentedConfig config, String categoryName, Object category, Map<String, Object> fallbackValues) throws
		IllegalAccessException {
		CommentedConfig subConfig = config.get(categoryName) instanceof CommentedConfig cc ? cc : null;
		for (var field : FieldAccess.collectOrderedFields(category.getClass())) {
			field.setAccessible(true);
			if (field.isAnnotationPresent(Category.class)) {
				readCategory(subConfig != null ? subConfig : config, field.getName(), field.get(category), fallbackValues);
				continue;
			}
			Object value = subConfig != null ? subConfig.get(field.getName()) : null;
			if (value == null && fallbackValues != null) value = fallbackValues.get(field.getName());
			if (value == null) continue;
			// 单条非法或类型不符时跳过该条并保留默认值，避免一个坏值把整份配置拖回默认
			try {
				if (!applyValue(category, field, value)) LOGGER.warn(
					"Ignoring option '{}': value '{}' does not match type {}",
					field.getName(),
					value,
					field.getType().getSimpleName()
				);
			} catch (Exception e) {
				LOGGER.warn("Ignoring option '{}': failed to read value '{}'", field.getName(), value, e);
			}
		}
	}
	public static class Builder<C> {
		private final Class<C> configClass;
		private Builder(Class<C> configClass) {
			this.configClass = configClass;
		}
		public ConfigSerializer<C> build() {
			return new ConfigSerializer<>(this);
		}
	}
}
