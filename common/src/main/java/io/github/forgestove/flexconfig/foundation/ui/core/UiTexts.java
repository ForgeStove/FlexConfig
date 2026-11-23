package io.github.forgestove.flexconfig.foundation.ui.core;
import io.github.forgestove.flexconfig.foundation.ui.core.UiText.*;

import java.util.*;
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
		private static String string(UiText text) {
			if (text instanceof Literal literal) return literal.text();
			if (text instanceof Translatable translatable) return translatable.key();
			if (text instanceof Native nativeText) return String.valueOf(nativeText.value());
			if (text instanceof Composite composite) return string(composite.left()) + string(composite.right());
			throw new IllegalStateException("Unknown UiText type: " + text.getClass().getName());
		}
		@Override
		public String getString(UiText text) {
			return string(text);
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
