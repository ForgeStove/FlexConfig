package io.github.forgestove.flexconfig.foundation.ui.core;
import java.util.List;
/**
 * {@link UiText} 的实现接口。文本的具体表示由实现决定，界面层只通过 UiText 的方法操作它。
 */
public interface UiTextEngine {
	UiText literal(String text);
	UiText translatable(String key, Object... args);
	UiText translatableWithFallback(String fallback, String key, Object... args);
	/** 参数用 Object，具体文本类型只在实现中出现。 */
	UiText fromComponent(Object component);
	UiText copy(UiText text);
	UiText append(UiText text, UiText sibling);
	UiText append(UiText text, String literal);
	UiText withStyle(UiText text, UiStyle style);
	/** 替换文本内容并保留原样式，用于大写转换这类不改变语义的变换。 */
	UiText replaceText(UiText text, String content);
	String getString(UiText text);
	int width(UiText text);
	int width(String text);
	int lineHeight();
	String trim(String text, int maxWidth);
	List<UiText> split(UiText text, int maxWidth);
}
