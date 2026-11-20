package io.github.forgestove.flexconfig.foundation.ui.core;
import java.util.*;
/**
 * {@link UiText} 的静态入口，保存当前安装的实现。兼容层启动时安装 MinecraftTextEngine，
 * 界面层只调用这里的方法。
 */
public final class UiTexts {
	private static UiTextEngine engine = Fallback.INSTANCE;
	private UiTexts() {}
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
		private record Plain(String text, UiStyle style) {}
		private static String textOf(UiText text) {
			return text.handle() instanceof Plain plain ? plain.text() : String.valueOf(text.handle());
		}
		@Override
		public UiText literal(String text) {
			return UiText.of(new Plain(text, UiStyle.EMPTY));
		}
		@Override
		public UiText translatable(String key, Object... args) {
			return UiText.of(new Plain(key, UiStyle.EMPTY));
		}
		@Override
		public UiText translatableWithFallback(String fallback, String key, Object... args) {
			return UiText.of(new Plain(fallback, UiStyle.EMPTY));
		}
		@Override
		public UiText fromComponent(Object component) {
			return UiText.of(new Plain(String.valueOf(component), UiStyle.EMPTY));
		}
		@Override
		public UiText copy(UiText text) {
			return text;
		}
		@Override
		public UiText append(UiText text, UiText sibling) {
			return UiText.of(new Plain(textOf(text) + textOf(sibling), UiStyle.EMPTY));
		}
		@Override
		public UiText append(UiText text, String literal) {
			return UiText.of(new Plain(textOf(text) + literal, UiStyle.EMPTY));
		}
		@Override
		public UiText withStyle(UiText text, UiStyle style) {
			return UiText.of(new Plain(textOf(text), style));
		}
		@Override
		public UiText replaceText(UiText text, String content) {
			return UiText.of(new Plain(content, UiStyle.EMPTY));
		}
		@Override
		public String getString(UiText text) {
			return textOf(text);
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
			for (var line : value.split("\n", -1)) lines.add(literal(line));
			return lines;
		}
	}
}
