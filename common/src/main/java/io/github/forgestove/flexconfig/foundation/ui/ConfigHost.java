package io.github.forgestove.flexconfig.foundation.ui;
import io.github.forgestove.flexconfig.api.menu.Accent;
public interface ConfigHost {
	int guiWidth();
	int guiHeight();
	boolean shiftDown();
	void close();
	Accent accent();
}
