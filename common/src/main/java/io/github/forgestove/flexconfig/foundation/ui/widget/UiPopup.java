package io.github.forgestove.flexconfig.foundation.ui.widget;
import io.github.forgestove.flexconfig.foundation.ui.core.UiContext;
public interface UiPopup {
	void renderPopup(UiContext ui);
	boolean isOverPopup(double mouseX, double mouseY);
	boolean popupMouseClicked(UiContext ui, double mouseX, double mouseY, int button);
	boolean popupMouseReleased(UiContext ui, double mouseX, double mouseY, int button);
	boolean popupMouseScrolled(double amount);
	boolean popupKeyPressed(UiContext ui, int keyCode, int scanCode, int modifiers);
	boolean popupCharTyped(UiContext ui, char codePoint);
	void close();
	boolean closing();
}
