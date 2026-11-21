package io.github.forgestove.flexconfig.foundation.client;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.*;
/**
 * UiText 的 Minecraft 实现。把平台无关的文本描述转成 Component；翻译、度量、换行和样式都交给
 * Component 和 Font 处理。
 */
public final class MinecraftTextEngine implements UiTextEngine {
	public static final MinecraftTextEngine INSTANCE = new MinecraftTextEngine();
	static {
		UiTexts.install(INSTANCE);
	}
	private static final UiTextVisitor<Component> COMPONENT = new UiTextVisitor<>() {
		@Override
		public Component literal(String text, UiStyle style) {
			return Component.literal(text).withStyle(style(style));
		}
		@Override
		public Component translatable(String key, @Nullable String fallback, Object[] args, UiStyle style) {
			var converted = args(args);
			var result = fallback == null
				? Component.translatable(key, converted)
				: Component.translatableWithFallback(key, fallback, converted);
			return result.withStyle(style(style));
		}
		@Override
		public Component nativeText(Object value, UiStyle style) {
			var result = value instanceof Component component ? component : Component.literal(String.valueOf(value));
			return style == UiStyle.EMPTY ? result : result.copy().withStyle(style(style));
		}
		@Override
		public Component composite(Component left, Component right, UiStyle style) {
			return Component.empty().withStyle(style(style)).append(left).append(right);
		}
	};
	private static Component component(UiText text) {
		return text.visit(COMPONENT);
	}
	public static Component toComponent(UiText text) {
		return component(text);
	}
	/** 绘制统一走 {@code FormattedCharSequence}，样式由它逐字符携带。 */
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
	/** 按字符样式分段还原，断行结果转回 Component 后仍能继续复制、追加和改样式。 */
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
