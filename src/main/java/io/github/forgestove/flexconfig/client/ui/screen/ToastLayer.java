package io.github.forgestove.flexconfig.client.ui.screen;
import io.github.forgestove.flexconfig.client.ui.core.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
/** 底部居中的提示条：左侧色条、可选的次级按钮动作，以及倒计时进度。 */
public class ToastLayer {
	private static final int HEIGHT = 20, PAD = 6, MAX_WIDTH = 315;
	private record Message(Component text, @Nullable Component actionLabel, @Nullable Runnable action, boolean undo) {}
	private final UiTween in = new UiTween(0);
	private final UiTween swap = new UiTween(1);
	private @Nullable Message current;
	private @Nullable Message previous;
	private UiBounds bounds = UiBounds.EMPTY;
	private float remainingMs, totalMs;
	private boolean leaving, pressed;
	public void info(Component text) {
		show(new Message(text, null, null, false));
	}
	public void info(Component text, Component actionLabel, Runnable action) {
		show(new Message(text, actionLabel, action, true));
	}
	private void show(Message next) {
		if (current != null && !leaving) {
			previous = current;
			swap.start(0, 1, UiMotion.POPUP_IN);
		} else {
			previous = null;
			swap.snap(1);
			in.start(in.value(), 1, UiMotion.POPUP_IN);
		}
		current = next;
		leaving = false;
		totalMs = next.undo() ? 8000F : 6000F;
		remainingMs = totalMs;
	}
	public void dismiss() {
		if (current == null || leaving) return;
		leaving = true;
		in.start(in.value(), 0, UiMotion.POPUP_OUT);
	}
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (current == null || leaving || button != 0 || current.action() == null || !bounds.contains(mouseX, mouseY)) return false;
		pressed = true;
		return true;
	}
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (!pressed) return false;
		pressed = false;
		if (current == null || leaving || button != 0 || current.action() == null || !bounds.contains(mouseX, mouseY)) return true;
		var action = current.action();
		dismiss();
		action.run();
		return true;
	}
	public void update(UiContext ui, float delta) {
		in.update(delta);
		swap.update(delta);
		if (swap.done()) previous = null;
		if (current == null) return;
		if (leaving) {
			if (in.done()) {
				current = null;
				previous = null;
				leaving = false;
			}
			return;
		}
		if (!pressed && !bounds.contains(ui.mouseX, ui.mouseY)) {
			remainingMs -= delta * 1000F;
			if (remainingMs <= 0F) dismiss();
		}
	}
	public void render(UiContext ui, int screenWidth, int screenHeight) {
		if (current == null) return;
		var painter = ui.painter;
		if (previous != null) {
			painter.pushAlpha(in.value() * (1F - swap.value()));
			draw(ui, previous, screenWidth, screenHeight, false);
			painter.popAlpha();
		}
		painter.pushAlpha(in.value() * (previous == null ? 1F : swap.value()));
		painter.pushTranslate(0, (1F - in.value()) * 4F);
		draw(ui, current, screenWidth, screenHeight, true);
		painter.popTranslate();
		painter.popAlpha();
	}
	private void draw(UiContext ui, Message message, int screenWidth, int screenHeight, boolean live) {
		var painter = ui.painter;
		var action = message.actionLabel();
		var buttonWidth = action == null ? 0 : painter.textWidth(action) + 10;
		var actionWidth = action == null ? 0 : buttonWidth + 6;
		var boxLimit = Math.min(MAX_WIDTH, screenWidth - 8);
		var maxText = Math.max(6, boxLimit - 3 - 12 - actionWidth);
		var text = painter.trim(message.text().getString(), maxText);
		var width = Math.min(screenWidth - 8, 9 + painter.textWidth(text) + 6 + actionWidth);
		var x = (screenWidth - width) / 2;
		var y = screenHeight - HEIGHT - PAD;
		if (live) bounds = new UiBounds(x, y, width, HEIGHT);
		painter.rect(x + 2, y + 2, width, HEIGHT, UiTheme.SHADOW);
		painter.border(x, y, width, HEIGHT, UiTheme.WINDOW_EDGE);
		painter.rect(x + 1, y + 1, width - 2, 1, UiTheme.GROOVE_LIGHT);
		painter.rect(x + 1, y + 2, width - 2, HEIGHT - 3, UiTheme.SIDEBAR);
		painter.rect(x + 1, y + 1, 3, HEIGHT - 2, ui.accent().base());
		painter.text(text, x + 10, y + 7, UiTheme.TEXT);
		if (action != null) {
			var hovered = live && bounds.contains(ui.mouseX, ui.mouseY);
			var down = live && pressed;
			var buttonX = x + width - PAD - buttonWidth;
			var faceColor = down ? UiTheme.SECONDARY_PRESSED : hovered ? UiTheme.SECONDARY_HOVER : UiTheme.SECONDARY_FACE;
			var face = painter.bevel(
				new UiBounds(buttonX, y + 3, buttonWidth, HEIGHT - 5),
				UiTheme.WINDOW_EDGE,
				faceColor,
				UiTheme.SECONDARY_TOP,
				UiTheme.SECONDARY_SHINE,
				UiTheme.SECONDARY_LIP,
				2,
				down
			);
			painter.text(action, buttonX + 5, face.y() + (face.height() - 7) / 2, UiTheme.TITLE_INK);
		}
		if (live && remainingMs > 0F) {
			var lineWidth = Math.round((width - 6) * (remainingMs / Math.max(1F, totalMs)));
			painter.rect(x + 4, y + HEIGHT - 2, lineWidth, 1, UiTheme.mulAlpha(ui.accent().base(), 0.6F));
		}
	}
}
