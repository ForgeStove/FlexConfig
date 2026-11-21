package io.github.forgestove.flexconfig.foundation.ui.core;
import org.jetbrains.annotations.Nullable;
/**
 * 记录指针按下的目标。按下时保存，释放时直接交给它，不再从控件列表里查找谁处于按下状态；
 * 列表在按压期间重排也不会把释放事件发给别的控件。
 */
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
