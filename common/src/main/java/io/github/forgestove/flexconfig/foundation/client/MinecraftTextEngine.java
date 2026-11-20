package io.github.forgestove.flexconfig.foundation.client;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;

import java.util.*;
/**
 * UiText 的 Minecraft 实现。翻译、字体度量、换行和样式都交给 Component 和 Font 处理；
 * 断行结果保留为 FormattedCharSequence，绘制时才能保留每行的样式。
 */
public final class MinecraftTextEngine implements UiTextEngine {
	public static final MinecraftTextEngine INSTANCE = new MinecraftTextEngine();
	static {
		UiTexts.install(INSTANCE);
	}
	private MinecraftTextEngine() {}
	private static Component component(UiText text) {
		return text.handle() instanceof Component component ? component : Component.literal(INSTANCE.getString(text));
	}
	public static Component toComponent(UiText text) {
		return component(text);
	}
	/** 断行结果是 {@code FormattedCharSequence}，直接画才保留样式。 */
	public static Object toRenderable(UiText text) {
		var handle = text.handle();
		return handle instanceof Component || handle instanceof FormattedCharSequence
			? handle
			: Component.literal(INSTANCE.getString(text));
	}
	private static Style style(UiStyle style) {
		var result = Style.EMPTY;
		if (style.color() != null) result = result.withColor(style.color());
		if (style.bold() != null) result = result.withBold(style.bold());
		if (style.italic() != null) result = result.withItalic(style.italic());
		if (style.underlined() != null) result = result.withUnderlined(style.underlined());
		if (style.strikethrough() != null) result = result.withStrikethrough(style.strikethrough());
		if (style.obfuscated() != null) result = result.withObfuscated(style.obfuscated());
		return result;
	}
	private static Object[] args(Object[] args) {
		var converted = new Object[args.length];
		for (var i = 0; i < args.length; i++) converted[i] = args[i] instanceof UiText text ? toComponent(text) : args[i];
		return converted;
	}
	@Override
	public UiText literal(String text) {
		return UiText.of(Component.literal(text));
	}
	@Override
	public UiText translatable(String key, Object... args) {
		return UiText.of(Component.translatable(key, args(args)));
	}
	@Override
	public UiText translatableWithFallback(String fallback, String key, Object... args) {
		return UiText.of(Component.translatableWithFallback(key, fallback, args(args)));
	}
	@Override
	public UiText fromComponent(Object component) {
		if (component instanceof Component || component instanceof FormattedCharSequence) return UiText.of(component);
		return UiText.literal(String.valueOf(component));
	}
	@Override
	public UiText copy(UiText text) {
		return UiText.of(component(text).copy());
	}
	@Override
	public UiText append(UiText text, UiText sibling) {
		return UiText.of(component(text).copy().append(toComponent(sibling)));
	}
	@Override
	public UiText append(UiText text, String literal) {
		return UiText.of(component(text).copy().append(literal));
	}
	@Override
	public UiText withStyle(UiText text, UiStyle style) {
		return UiText.of(component(text).copy().withStyle(style(style)));
	}
	@Override
	public UiText replaceText(UiText text, String content) {
		return UiText.of(Component.literal(content).withStyle(component(text).getStyle()));
	}
	@Override
	public String getString(UiText text) {
		var handle = text.handle();
		if (handle instanceof Component component) return component.getString();
		if (handle instanceof FormattedCharSequence sequence) {
			var builder = new StringBuilder();
			sequence.accept((index, style, codePoint) -> {
				builder.appendCodePoint(codePoint);
				return true;
			});
			return builder.toString();
		}
		return String.valueOf(handle);
	}
	@Override
	public int width(UiText text) {
		var handle = text.handle();
		if (handle instanceof Component component) return ClientUtil.mc.font.width(component);
		if (handle instanceof FormattedCharSequence sequence) return ClientUtil.mc.font.width(sequence);
		return ClientUtil.mc.font.width(getString(text));
	}
	@Override
	public int width(String text) {
		return ClientUtil.mc.font.width(text);
	}
	@Override
	public int lineHeight() {
		return ClientUtil.mc.font.lineHeight;
	}
	@Override
	public String trim(String text, int maxWidth) {
		return ClientUtil.mc.font.plainSubstrByWidth(text, maxWidth);
	}
	@Override
	public List<UiText> split(UiText text, int maxWidth) {
		var handle = text.handle();
		var source = handle instanceof Component component ? component : Component.literal(getString(text));
		var lines = ClientUtil.mc.font.split(source, maxWidth);
		var out = new ArrayList<UiText>(lines.size());
		for (var line : lines) out.add(UiText.of(line));
		return out;
	}
}
