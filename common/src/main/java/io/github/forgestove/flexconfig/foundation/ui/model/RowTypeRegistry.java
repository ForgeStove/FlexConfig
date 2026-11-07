package io.github.forgestove.flexconfig.foundation.ui.model;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.flexconfig.foundation.ui.row.*;
import io.github.forgestove.flexconfig.foundation.tree.ValueConfigNode;
import net.minecraft.network.chat.Component;

import java.awt.Point;
import java.util.*;
/** 类型到行的映射。 */
public class RowTypeRegistry {
	private static final Map<Class<?>, Factory> defaults = new LinkedHashMap<>();
	static {
		register(Boolean.class, BooleanRow::new);
		register(Integer.class, RowTypeRegistry::integerRow);
		register(Long.class, (ctx, node) -> node.getSliderRange() != null
			? new SliderRow(ctx, node, Math::round)
			: new NumberRow(ctx, node, RowTypeRegistry::parseLong, RowTypeRegistry::longText, String::valueOf));
		register(Float.class, (ctx, node) -> node.getSliderRange() != null
			? new SliderRow(ctx, node, value -> (float) value)
			: new NumberRow(ctx, node, RowTypeRegistry::parseFloat, RowTypeRegistry::floatText, String::valueOf));
		register(Double.class, (ctx, node) -> node.getSliderRange() != null
			? new SliderRow(ctx, node, value -> value)
			: new NumberRow(ctx, node, RowTypeRegistry::parseDouble, RowTypeRegistry::doubleText, String::valueOf));
		register(Byte.class, (ctx, node) -> new NumberRow(ctx, node, RowTypeRegistry::parseByte, RowTypeRegistry::integerText, String::valueOf));
		register(Short.class, (ctx, node) -> new NumberRow(ctx, node, RowTypeRegistry::parseShort, RowTypeRegistry::integerText, String::valueOf));
		register(Character.class, RowTypeRegistry::characterRow);
		register(Enum.class, RowTypeRegistry::enumRow);
		register(String.class, (ctx, node) -> new TextRow(ctx, node, s -> s, s -> true, String::valueOf));
		register(Key.class, KeybindRow::new);
		register(Point.class, PointRow::new);
		register(List.class, RowTypeRegistry::collectionPlaceholder);
	}
	public static void register(Class<?> type, Factory factory) {
		defaults.put(type, factory);
	}
	public static ConfigRow create(RowContext ctx, ValueConfigNode<?, ?> node) {
		for (var entry : defaults.entrySet()) if (entry.getKey().isAssignableFrom(node.getValueType())) return entry.getValue().create(ctx, node);
		return new UnsupportedTypeRow(ctx, node, 0);
	}
	private static ConfigRow integerRow(RowContext ctx, ValueConfigNode<?, ?> node) {
		if (node.isColorValue()) return new ColorRow(ctx, node);
		if (node.getSliderRange() != null) return new SliderRow(ctx, node, value -> (int) Math.round(value));
		return new NumberRow(ctx, node, RowTypeRegistry::parseInt, RowTypeRegistry::integerText, String::valueOf);
	}
	private static ConfigRow characterRow(RowContext ctx, ValueConfigNode<?, ?> node) {
		return new TextRow(ctx, node, s -> s.isEmpty() ? '\0' : s.charAt(0), s -> s.length() <= 1, c -> c == null || (Character) c == '\0' ? "" : String.valueOf(c), 1);
	}
	private static ConfigRow enumRow(RowContext ctx, ValueConfigNode<?, ?> node) {
		return new EnumRow(ctx, node);
	}
	private static ConfigRow collectionPlaceholder(RowContext ctx, ValueConfigNode<?, ?> node) {
		return new InfoRow(ctx, placeholder(ctx, node), 0, false);
	}
	private static Component placeholder(RowContext ctx, ValueConfigNode<?, ?> node) {
		return node.getTitle().copy().append(Component.literal(": ")).append(Component.literal(String.valueOf(rawValue(ctx, node))));
	}
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static Object rawValue(RowContext ctx, ValueConfigNode node) {
		return node.getEditingValue(ctx.config());
	}
	private static boolean isZero(String s) {
		return s.isEmpty() || s.equals("-");
	}
	private static Object parseInt(String s) {
		return isZero(s) ? 0 : Integer.parseInt(s);
	}
	private static boolean integerText(String s) {
		return isZero(s) || s.matches("-?\\d*");
	}
	private static Object parseLong(String s) {
		return isZero(s) ? 0L : Long.parseLong(s);
	}
	private static boolean longText(String s) {
		return isZero(s) || s.matches("-?\\d*");
	}
	private static Object parseFloat(String s) {
		return isZero(s) ? 0F : Float.parseFloat(s);
	}
	private static boolean floatText(String s) {
		return isZero(s) || s.matches("-?\\d*(\\.\\d*)?");
	}
	private static Object parseDouble(String s) {
		return isZero(s) ? 0D : Double.parseDouble(s);
	}
	private static boolean doubleText(String s) {
		return isZero(s) || s.matches("-?\\d*(\\.\\d*)?");
	}
	private static Object parseByte(String s) {
		return isZero(s) ? (byte) 0 : Byte.parseByte(s);
	}
	private static Object parseShort(String s) {
		return isZero(s) ? (short) 0 : Short.parseShort(s);
	}
	@FunctionalInterface
	public interface Factory {
		ConfigRow create(RowContext ctx, ValueConfigNode<?, ?> node);
	}
}
