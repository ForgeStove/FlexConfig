package io.github.forgestove.flexconfig.foundation.ui.core;
public interface UiPointerTarget {
	boolean mouseClicked(UiContext ctx, double mouseX, double mouseY, int button);
	/** 记录按下时的位置，释放时用来判断鼠标是否还在原处。 */
	default void markPressed() {}
	default boolean mouseDragged(UiContext ctx, double mouseX, double mouseY, int button, double dragX, double dragY) {
		return false;
	}
	default boolean mouseReleased(UiContext ctx, double mouseX, double mouseY, int button) {
		return false;
	}
}
