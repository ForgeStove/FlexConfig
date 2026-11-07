package io.github.forgestove.flexconfig.foundation.ui;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
/** 界面编排层需要的 Screen 环境：尺寸、父屏、切屏和 Shift 状态。 */
public interface ConfigHost {
	int guiWidth();
	int guiHeight();
	boolean shiftDown();
	Screen parentScreen();
	void setScreen(@Nullable Screen screen);
	void copyToClipboard(String text);
}
