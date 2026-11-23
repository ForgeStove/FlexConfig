package io.github.forgestove.flexconfig.foundation.ui.core;
import java.util.List;
public interface UiTextEngine {
	String getString(UiText text);
	int width(UiText text);
	int width(String text);
	int lineHeight();
	String trim(String text, int maxWidth);
	List<UiText> split(UiText text, int maxWidth);
}
