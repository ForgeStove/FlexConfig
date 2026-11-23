package io.github.forgestove.flexconfig.foundation.ui.factory;
import io.github.forgestove.flexconfig.foundation.ui.core.UiStyle;
public enum KeybindState {
	UNBOUND(UiStyle.DARK_GRAY),
	CONFLICT(UiStyle.YELLOW),
	BOUND(UiStyle.GREEN);
	private final UiStyle color;
	KeybindState(UiStyle color) {
		this.color = color;
	}
	public UiStyle color() {
		return color;
	}
}
