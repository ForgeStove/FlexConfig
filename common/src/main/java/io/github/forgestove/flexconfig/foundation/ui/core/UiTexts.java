package io.github.forgestove.flexconfig.foundation.ui.core;
import org.jetbrains.annotations.Nullable;

import java.util.*;
/**
 * {@link UiText} 的静态读取入口，保存当前安装的实现。实现只影响文本的翻译和度量，
 * 文本对象本身在创建时不依赖它。
 */
public final class UiTexts {
	private static UiTextEngine engine = Fallback.INSTANCE;
	public static void install(UiTextEngine engine) {
		if (engine != null) UiTexts.engine = engine;
	}
	public static UiTextEngine engine() {
		return engine;
	}
	public static int width(UiText text) {
		return engine.width(text);
	}
	public static int width(String text) {
		return engine.width(text);
	}
	public static int lineHeight() {
		return engine.lineHeight();
	}
	public static String trim(String text, int maxWidth) {
		return engine.trim(text, maxWidth);
	}
	public static List<UiText> split(UiText text, int maxWidth) {
		return engine.split(text, maxWidth);
	}
	/** 实现安装前使用的兜底：只做字符串拼接和近似宽度，保证界面代码不会因为后端缺失而崩溃。 */
	private enum Fallback implements UiTextEngine {
		INSTANCE;
		private static final UiTextVisitor<String> STRING = new UiTextVisitor<>() {
			@Override
			public String literal(String text, UiStyle style) {
				return text;
			}
			@Override
			public String translatable(String key, @Nullable String fallback, Object[] args, UiStyle style) {
				return key;
			}
			@Override
			public String nativeText(Object value, UiStyle style) {
				return String.valueOf(value);
			}
			@Override
			public String composite(String left, String right, UiStyle style) {
				return left + right;
			}
		};
		@Override
		public String getString(UiText text) {
			return text.visit(STRING);
		}
		@Override
		public int width(UiText text) {
			return width(getString(text));
		}
		@Override
		public int width(String text) {
			return text.length() * 6;
		}
		@Override
		public int lineHeight() {
			return 9;
		}
		@Override
		public String trim(String text, int maxWidth) {
			var max = Math.max(0, maxWidth / 6);
			return text.length() <= max ? text : text.substring(0, max);
		}
		@Override
		public List<UiText> split(UiText text, int maxWidth) {
			var value = getString(text);
			if (value.isEmpty()) return List.of();
			var lines = new ArrayList<UiText>();
			for (var line : value.split("\n", -1)) lines.add(UiText.literal(line));
			return lines;
		}
	}
}
