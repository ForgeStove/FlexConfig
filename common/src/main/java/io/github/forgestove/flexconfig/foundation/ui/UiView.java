package io.github.forgestove.flexconfig.foundation.ui;
import io.github.forgestove.flexconfig.foundation.ui.core.UiRenderTarget;
import org.jetbrains.annotations.NotNull;
/**
 * 界面对屏幕层暴露的生命周期和输入方法。客户端的 Screen 适配器把原版回调转发到这里，
 * 具体的绘制和输入分派由实现负责。
 */
public interface UiView {
	void init();
	void resize();
	void frame(@NotNull UiRenderTarget target, int mouseX, int mouseY, float seconds);
	boolean input(ConfigInput event);
	void onClose();
	void onRemoved();
	void refresh();
	boolean closed();
}
