package io.github.forgestove.flexconfig.foundation.ui.core;
import java.util.List;
/**
 * {@link UiText} 的读取实现。文本的描述由 UiText 自身保存，引擎只负责翻译、度量、裁剪和断行，
 * 引擎在何时安装不影响已经创建的文本。
 */
public interface UiTextEngine {
	String getString(UiText text);
	int width(UiText text);
	int width(String text);
	int lineHeight();
	String trim(String text, int maxWidth);
	List<UiText> split(UiText text, int maxWidth);
}
