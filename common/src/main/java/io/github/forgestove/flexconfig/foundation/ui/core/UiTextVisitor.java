package io.github.forgestove.flexconfig.foundation.ui.core;
import org.jetbrains.annotations.Nullable;
/** 把 {@link UiText} 的平台无关描述解释成实现自己的表示。 */
public interface UiTextVisitor<R> {
	R literal(String text, UiStyle style);
	R translatable(String key, @Nullable String fallback, Object[] args, UiStyle style);
	R nativeText(Object value, UiStyle style);
	R composite(R left, R right, UiStyle style);
}
