package io.github.forgestove.flexconfig.foundation.ui;
import io.github.forgestove.flexconfig.api.registration.ConfigHandler;

import java.util.function.IntConsumer;
public interface ConfigActions extends ConfigHost {
	void openConfig(ConfigHandler<?, ?> handler);
	void openColorPicker(int color, boolean alpha, IntConsumer onSelected);
	void copyToClipboard(String text);
	boolean inWorld();
	boolean localServer();
	void disconnectGame();
	void stopGame();
}
