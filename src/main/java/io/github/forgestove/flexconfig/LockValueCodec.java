package io.github.forgestove.flexconfig;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import org.jetbrains.annotations.Nullable;

import java.awt.Point;
import java.util.*;
public final class LockValueCodec {
	public static String format(ValueConfigNode<?, ?> node, Object value) {
		if (node.isColorValue()) return String.format(node.colorHasAlpha() ? "0x%08X" : "0x%06X", value);
		return switch (value) {
			case Enum<?> anEnum -> anEnum.name();
			case Point point -> point.x + ", " + point.y;
			case Collection<?> collection -> join(collection);
			default -> String.valueOf(value);
		};
	}
	private static String join(Collection<?> values) {
		var parts = new ArrayList<String>(values.size());
		for (var value : values) if (value != null) parts.add(String.valueOf(value));
		return String.join(", ", parts);
	}
	/** 空串、类型不支持或字面量解析失败时返回 {@code null}，调用方按「这条锁无效」处理。 */
	public static @Nullable Object toToml(ValueConfigNode<?, ?> node, String raw) {
		if (raw.isEmpty()) return null;
		var type = node.getValueType();
		if (node.isColorValue()) return toInt(raw);
		if (type == String.class) return raw;
		if (type == List.class) return toList(node.getElementType(), raw);
		if (type == Point.class) return toPoint(raw);
		if (type == Boolean.class) return raw.equals("true") ? Boolean.TRUE : raw.equals("false") ? Boolean.FALSE : null;
		if (type.isEnum()) return toEnum(type, raw);
		return toNumber(type, raw);
	}
	private static @Nullable Integer toInt(String raw) {
		try {
			return Integer.decode(raw);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	/** 接受逗号分隔与 {@code [a, b]} 两种写法，元素按 {@code List<T>} 的声明类型转换。 */
	private static @Nullable Object toList(@Nullable Class<?> elementType, String raw) {
		var body = raw.trim();
		if (body.startsWith("[") && body.endsWith("]")) body = body.substring(1, body.length() - 1).trim();
		if (body.isEmpty()) return List.of();
		var result = new ArrayList<>();
		for (var part : body.split(",")) {
			var token = part.trim();
			if (token.isEmpty()) continue;
			var element = elementType == null || elementType == String.class ? token : toElement(elementType, token);
			if (element == null) return null;
			result.add(element);
		}
		return result;
	}
	/** {@code "x, y"} → 子表 {@code {x, y}}：点字段在 TOML 中即以此形式存储，读侧 {@code ConfigSerializer} 按子表读取。 */
	private static @Nullable Object toPoint(String raw) {
		var parts = raw.split(",");
		if (parts.length != 2) return null;
		try {
			var point = ConfigSerializer.inMemory();
			point.set("x", Integer.parseInt(parts[0].trim()));
			point.set("y", Integer.parseInt(parts[1].trim()));
			return point;
		} catch (NumberFormatException e) {
			return null;
		}
	}
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static @Nullable Object toEnum(Class<?> type, String raw) {
		try {
			return Enum.valueOf((Class<? extends Enum>) type, raw);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	private static @Nullable Object toNumber(Class<?> type, String raw) {
		try {
			if (type == Integer.class) return Integer.decode(raw);
			if (type == Long.class) return Long.decode(raw);
			if (type == Float.class) return Float.parseFloat(raw);
			if (type == Double.class) return Double.parseDouble(raw);
		} catch (NumberFormatException ignored) {}
		return null;
	}
	private static @Nullable Object toElement(Class<?> elementType, String token) {
		if (elementType == Boolean.class) return token.equals("true") ? Boolean.TRUE : token.equals("false") ? Boolean.FALSE : null;
		if (elementType.isEnum()) return toEnum(elementType, token);
		return toNumber(elementType, token);
	}
}
