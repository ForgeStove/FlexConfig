package io.github.forgestove.flexconfig.foundation.ui.core;
import org.jetbrains.annotations.Nullable;
/** 文本样式。字段为 null 表示沿用上层样式，只设置需要改的部分。 */
public record UiStyle(
	@Nullable Integer color,
	@Nullable Boolean bold,
	@Nullable Boolean italic,
	@Nullable Boolean underlined,
	@Nullable Boolean strikethrough,
	@Nullable Boolean obfuscated
) {
	public static final UiStyle EMPTY = new UiStyle(null, null, null, null, null, null);
	public static final UiStyle BOLD = EMPTY.withBold(true);
	public static final UiStyle UNDERLINE = EMPTY.withUnderlined(true);
	public static final UiStyle WHITE = EMPTY.withColor(0xFFFFFF);
	public static final UiStyle GRAY = EMPTY.withColor(0xAAAAAA);
	public static final UiStyle DARK_GRAY = EMPTY.withColor(0x555555);
	public static final UiStyle RED = EMPTY.withColor(0xFF5555);
	public static final UiStyle GREEN = EMPTY.withColor(0x55FF55);
	public static final UiStyle YELLOW = EMPTY.withColor(0xFFFF55);
	public UiStyle withColor(int rgb) {
		return new UiStyle(rgb & 0xFFFFFF, bold, italic, underlined, strikethrough, obfuscated);
	}
	public UiStyle withBold(boolean bold) {
		return new UiStyle(color, bold, italic, underlined, strikethrough, obfuscated);
	}
	public UiStyle withItalic(boolean italic) {
		return new UiStyle(color, bold, italic, underlined, strikethrough, obfuscated);
	}
	public UiStyle withUnderlined(boolean underlined) {
		return new UiStyle(color, bold, italic, underlined, strikethrough, obfuscated);
	}
	public UiStyle withStrikethrough(boolean strikethrough) {
		return new UiStyle(color, bold, italic, underlined, strikethrough, obfuscated);
	}
	public UiStyle withObfuscated(boolean obfuscated) {
		return new UiStyle(color, bold, italic, underlined, strikethrough, obfuscated);
	}
}
