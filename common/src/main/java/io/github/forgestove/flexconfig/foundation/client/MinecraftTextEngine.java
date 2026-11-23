package io.github.forgestove.flexconfig.foundation.client;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.core.UiText.Composite;
import io.github.forgestove.flexconfig.foundation.ui.core.UiText.Literal;
import io.github.forgestove.flexconfig.foundation.ui.core.UiText.Native;
import io.github.forgestove.flexconfig.foundation.ui.core.UiText.Translatable;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;

import java.util.*;
public final class MinecraftTextEngine implements UiTextEngine {
	public static final MinecraftTextEngine INSTANCE = new MinecraftTextEngine();
	static {
		UiTexts.install(INSTANCE);
	}
	private static Component component(UiText text) {
		if (text instanceof Literal literal) return Component.literal(literal.text()).withStyle(style(literal.style()));
		if (text instanceof Translatable translatable) {
			var converted = args(translatable.args());
			var result = translatable.fallback() == null
				? Component.translatable(translatable.key(), converted)
				: Component.translatableWithFallback(translatable.key(), translatable.fallback(), converted);
			return result.withStyle(style(translatable.style()));
		}
		if (text instanceof Native nativeText) {
			var value = nativeText.value();
			var result = value instanceof Component component ? component : Component.literal(String.valueOf(value));
			return nativeText.style() == UiStyle.EMPTY ? result : result.copy().withStyle(style(nativeText.style()));
		}
		if (text instanceof Composite composite) return Component.empty()
			.withStyle(style(composite.style()))
			.append(component(composite.left()))
			.append(component(composite.right()));
		throw new IllegalStateException("Unknown UiText type: " + text.getClass().getName());
	}
	public static Component toComponent(UiText text) {
		return component(text);
	}
	public static FormattedCharSequence toRenderable(UiText text) {
		return component(text).getVisualOrderText();
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
		for (var i = 0; i < args.length; i++) converted[i] = args[i] instanceof UiText text ? component(text) : args[i];
		return converted;
	}
	private static Component fromSequence(FormattedCharSequence sequence) {
		var result = Component.empty();
		var run = new StringBuilder();
		var runStyle = new Style[]{Style.EMPTY};
		sequence.accept((index, style, codePoint) -> {
			if (!style.equals(runStyle[0])) {
				if (!run.isEmpty()) result.append(Component.literal(run.toString()).withStyle(runStyle[0]));
				run.setLength(0);
				runStyle[0] = style;
			}
			run.appendCodePoint(codePoint);
			return true;
		});
		if (!run.isEmpty()) result.append(Component.literal(run.toString()).withStyle(runStyle[0]));
		return result;
	}
	@Override
	public String getString(UiText text) {
		return component(text).getString();
	}
	@Override
	public int width(UiText text) {
		return ClientUtil.mc.font.width(component(text));
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
		var lines = ClientUtil.mc.font.split(component(text), maxWidth);
		var out = new ArrayList<UiText>(lines.size());
		for (var line : lines) out.add(UiText.nativeText(fromSequence(line)));
		return out;
	}
}
