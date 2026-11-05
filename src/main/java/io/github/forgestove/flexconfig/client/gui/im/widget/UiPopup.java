package io.github.forgestove.flexconfig.client.gui.im.widget;
import io.github.forgestove.flexconfig.client.gui.im.core.UiContext;
/** 界面层全局唯一的弹层：下拉、行右键菜单、页面菜单都走这套接口。 */
public interface UiPopup {
	void renderPopup(UiContext ui);
	boolean isOverPopup(double mouseX, double mouseY);
	boolean popupMouseClicked(UiContext ui, double mouseX, double mouseY, int button);
	boolean popupMouseReleased(UiContext ui, double mouseX, double mouseY, int button);
	boolean popupMouseScrolled(double amount);
	boolean popupKeyPressed(UiContext ui, int keyCode, int scanCode, int modifiers);
	boolean popupCharTyped(UiContext ui, char codePoint);
	void close();
}
