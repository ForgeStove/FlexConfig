package io.github.forgestove.flexconfig.foundation.ui;
import io.github.forgestove.flexconfig.foundation.ui.core.UiRenderTarget;
import org.jetbrains.annotations.NotNull;
/**
 * 界面对屏幕层暴露的生命周期和输入方法。ConfigScreen 把 Minecraft 的 Screen 回调转发到这里，
 * 具体的绘制和输入分派由实现负责。
 */
public interface UiView {
	void init();
	void resize();
	void frame(@NotNull UiRenderTarget target, int mouseX, int mouseY, float delta);
	boolean input(ConfigInput event);
	void onClose();
	void onRemoved();
	void refresh();
	boolean closed();
}
