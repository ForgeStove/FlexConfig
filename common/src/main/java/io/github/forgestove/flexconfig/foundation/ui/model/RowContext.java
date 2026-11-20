package io.github.forgestove.flexconfig.foundation.ui.model;
import io.github.forgestove.flexconfig.api.FileKey;
import io.github.forgestove.flexconfig.api.registration.ConfigHandler;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiDropdown;
/** 行访问配置和界面动作的接口。行只依赖这里的方法，不直接依赖 ConfigShell。 */
public interface RowContext {
	ConfigHandler<?, ?> handler();
	Object config();
	FileKey fileKey();
	String modId();
	boolean editable();
	boolean lockable();
	void refresh();
	void rebuildTab();
	void beginCapture(CaptureTarget target);
	void endCapture(CaptureTarget target);
	void saveByKeybind();
	void exitRow(Object identity, Runnable action);
	void exitElements(String collectionPath, Runnable action);
	void openDropdown(UiDropdown dropdown);
	void closeDropdown();
}
