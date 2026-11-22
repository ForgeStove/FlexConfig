package io.github.forgestove.flexconfig.foundation.ui.core;
import org.jetbrains.annotations.Nullable;
public class UiPointerRouter {
	private @Nullable UiPointerTarget pressed;
	public boolean press(UiContext ctx, @Nullable UiPointerTarget target, double mouseX, double mouseY, int button) {
		if (target == null || !target.mouseClicked(ctx, mouseX, mouseY, button)) return false;
		target.markPressed();
		pressed = target;
		return true;
	}
	public boolean release(UiContext ctx, double mouseX, double mouseY, int button) {
		var target = pressed;
		pressed = null;
		var handled = target != null && target.mouseReleased(ctx, mouseX, mouseY, button);
		ctx.setDragOwner(null);
		return handled;
	}
	public @Nullable UiPointerTarget pressed() {
		return pressed;
	}
	public void clear() {
		pressed = null;
	}
}
