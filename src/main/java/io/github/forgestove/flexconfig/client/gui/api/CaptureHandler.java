package io.github.forgestove.flexconfig.client.gui.api;
/** 由 Screen 在键盘与鼠标事件里转发调用。 */
public interface CaptureHandler {
	boolean handleCaptureKey(int keyCode);
	boolean handleCaptureMouse(int button);
}
