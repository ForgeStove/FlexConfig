package io.github.forgestove.flexconfig.foundation.ui.core;
/**
 * 界面层使用的文本对象。内部保存实现提供的文本引用；翻译、度量、换行和样式修改都交给
 * {@link UiTexts} 的实现，界面代码不接触 Component。
 */
public final class UiText {
	private final Object handle;
	private UiText(Object handle) {
		this.handle = handle;
	}
	/** 由实现方调用，用它包装具体的文本对象。 */
	public static UiText of(Object handle) {
		return new UiText(handle);
	}
	public static UiText empty() {
		return UiTexts.engine().literal("");
	}
	public static UiText literal(String text) {
		return UiTexts.engine().literal(text);
	}
	public static UiText translatable(String key, Object... args) {
		return UiTexts.engine().translatable(key, args);
	}
	public static UiText translatableWithFallback(String fallback, String key, Object... args) {
		return UiTexts.engine().translatableWithFallback(fallback, key, args);
	}
	public static UiText from(Object component) {
		return component == null ? null : UiTexts.engine().fromComponent(component);
	}
	public Object handle() {
		return handle;
	}
	public UiText copy() {
		return UiTexts.engine().copy(this);
	}
	public UiText append(UiText sibling) {
		return UiTexts.engine().append(this, sibling);
	}
	public UiText append(String literal) {
		return UiTexts.engine().append(this, literal);
	}
	public UiText withStyle(UiStyle style) {
		return UiTexts.engine().withStyle(this, style);
	}
	/** 替换文本内容，保留原样式。 */
	public UiText replaceText(String content) {
		return UiTexts.engine().replaceText(this, content);
	}
	public String getString() {
		return UiTexts.engine().getString(this);
	}
	public int width() {
		return UiTexts.engine().width(this);
	}
	public boolean isEmpty() {
		return getString().isEmpty();
	}
}
