package io.github.forgestove.flexconfig.foundation.ui.model;
/** 键位捕获状态：界面在把按键/鼠标交给其他控件之前先询问它。 */
public interface CaptureTarget {
	boolean captureKey(int keyCode);
	void captureMouse(int button);
}
