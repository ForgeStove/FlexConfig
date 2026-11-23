package io.github.forgestove.flexconfig.foundation.ui.core;
import org.jetbrains.annotations.Nullable;
public sealed interface UiText {
	static UiText empty() {
		return literal("");
	}
	static UiText literal(String text) {
		return new Literal(text, UiStyle.EMPTY);
	}
	static UiText translatable(@com.demonwav.mcdev.annotations.Translatable(foldMethod = true) String key, Object... args) {
		return new Translatable(key, null, args, UiStyle.EMPTY);
	}
	static UiText translatableWithFallback(
		String fallback,
		@com.demonwav.mcdev.annotations.Translatable(foldMethod = true) String key,
		Object... args
	) {
		return new Translatable(key, fallback, args, UiStyle.EMPTY);
	}
	static UiText nativeText(Object value) {
		return new Native(value, UiStyle.EMPTY);
	}
	static @Nullable UiText from(@Nullable Object component) {
		return component == null ? null : nativeText(component);
	}
	UiStyle style();
	default UiText copy() {
		return this;
	}
	default UiText append(UiText sibling) {
		return new Composite(this, sibling, UiStyle.EMPTY);
	}
	default UiText append(String text) {
		return append(literal(text));
	}
	default UiText withStyle(UiStyle extra) {
		var merged = style().merge(extra);
		if (this instanceof Literal text) return new Literal(text.text(), merged);
		if (this instanceof Translatable text) return new Translatable(text.key(), text.fallback(), text.args(), merged);
		if (this instanceof Native text) return new Native(text.value(), merged);
		if (this instanceof Composite text) return new Composite(text.left(), text.right(), merged);
		throw new IllegalStateException("Unknown UiText type: " + getClass().getName());
	}
	default UiText replaceText(String content) {
		return new Literal(content, style());
	}
	default String getString() {
		return UiTexts.engine().getString(this);
	}
	default int width() {
		return UiTexts.engine().width(this);
	}
	default boolean isEmpty() {
		return getString().isEmpty();
	}
	record Literal(String text, UiStyle style) implements UiText {}
	record Translatable(String key, @Nullable String fallback, Object[] args, UiStyle style) implements UiText {}
	record Native(Object value, UiStyle style) implements UiText {}
	record Composite(UiText left, UiText right, UiStyle style) implements UiText {}
}
