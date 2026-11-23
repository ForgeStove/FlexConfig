package io.github.forgestove.flexconfig.foundation.ui.model;
import io.github.forgestove.flexconfig.api.FileKey;
import io.github.forgestove.flexconfig.api.registration.ConfigHandler;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiDropdown;

import java.util.function.IntConsumer;
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
	void openColorPicker(int color, boolean alpha, IntConsumer onSelected);
	void exitRow(Object identity, Runnable action);
	void exitElements(String collectionPath, Runnable action);
	void openDropdown(UiDropdown dropdown);
	void closeDropdown();
}
