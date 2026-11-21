package io.github.forgestove.flexconfig.foundation.ui;
import io.github.forgestove.flexconfig.api.registration.ConfigHandler;

import java.util.function.IntConsumer;
/** 界面视图可触发的跨屏动作和游戏会话操作，由客户端适配器实现。 */
public interface ConfigActions extends ConfigHost {
	void openConfig(ConfigHandler<?, ?> handler);
	void openColorPicker(int color, boolean alpha, IntConsumer onSelected);
	void copyToClipboard(String text);
	boolean inWorld();
	boolean localServer();
	void disconnectGame();
	void stopGame();
}
