package io.github.forgestove.flexconfig.foundation.ui.core;
import org.jetbrains.annotations.Nullable;
/**
 * 界面层使用的文本。内部只保存平台无关的文本描述，翻译、度量、换行和绘制由 {@link UiTexts}
 * 当前的实现按需解释，因此文本在何时创建与实现何时安装无关。
 */
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
	/** 包装实现提供的原生文本，具体类型只在实现中出现。 */
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
		return switch (this) {
			case Literal text -> new Literal(text.text(), merged);
			case Translatable text -> new Translatable(text.key(), text.fallback(), text.args(), merged);
			case Native text -> new Native(text.value(), merged);
			case Composite text -> new Composite(text.left(), text.right(), merged);
		};
	}
	/** 替换文本内容，保留原样式。 */
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
	/** 由实现解释文本描述；组合节点由实现递归解析。 */
	<R> R visit(UiTextVisitor<R> visitor);
	record Literal(String text, UiStyle style) implements UiText {
		@Override
		public <R> R visit(UiTextVisitor<R> visitor) {
			return visitor.literal(text, style);
		}
	}
	record Translatable(String key, @Nullable String fallback, Object[] args, UiStyle style) implements UiText {
		@Override
		public <R> R visit(UiTextVisitor<R> visitor) {
			return visitor.translatable(key, fallback, args, style);
		}
	}
	record Native(Object value, UiStyle style) implements UiText {
		@Override
		public <R> R visit(UiTextVisitor<R> visitor) {
			return visitor.nativeText(value, style);
		}
	}
	record Composite(UiText left, UiText right, UiStyle style) implements UiText {
		@Override
		public <R> R visit(UiTextVisitor<R> visitor) {
			return visitor.composite(left.visit(visitor), right.visit(visitor), style);
		}
	}
}
