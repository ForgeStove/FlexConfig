package io.github.forgestove.flexconfig.client.ui.widget;
import io.github.forgestove.flexconfig.client.ui.core.*;

import java.util.function.*;
public class UiToggle extends UiControl {
	private static final int TRACK_WIDTH = 30, TRACK_HEIGHT = 14, KNOB = 16, TRAVEL = 14;
	private final UiAnim knob = new UiAnim(0, UiMotion.CONTROL);
	private final UiAnim hover = new UiAnim(0);
	private final BooleanSupplier value;
	private final Consumer<Boolean> setter;
	public UiToggle(BooleanSupplier value, Consumer<Boolean> setter) {
		this.value = value;
		this.setter = setter;
		knob.jumpTo(value.getAsBoolean() ? TRAVEL : 0);
	}
	@Override
	public void render(UiContext ctx) {
		if (!visible) return;
		hover.target(focusable() && (hovered(ctx) || down) ? 1 : 0);
		hover.update(ctx.delta);
		knob.target(value.getAsBoolean() ? TRAVEL : 0);
		knob.update(ctx.delta);
		var painter = ctx.painter;
		var accent = ctx.accent();
		var tx = bounds().x() + (bounds().width() - TRACK_WIDTH) / 2;
		var ty = bounds().y() + (bounds().height() - TRACK_HEIGHT) / 2;
		var track = new UiBounds(tx, ty, TRACK_WIDTH, TRACK_HEIGHT);
		var knobX = tx + (int) Math.round(knob.get());
		var knobWidth = down ? 18 : KNOB;
		if (down && value.getAsBoolean()) knobX = Math.max(tx, knobX - 2);
		if (down && !value.getAsBoolean()) knobX = Math.min(knobX, tx + TRACK_WIDTH - knobWidth);
		painter.rect(track, UiTheme.WINDOW_EDGE);
		painter.rect(tx + 1, ty + 1, Math.max(0, knobX - tx - 1), TRACK_HEIGHT - 2, enabled ? accent.strong() : UiTheme.OFF_FILL);
		painter.rect(knobX + knobWidth, ty + 1, Math.max(0, tx + TRACK_WIDTH - 1 - knobX - knobWidth), TRACK_HEIGHT - 2, UiTheme.OFF_FILL);
		if (knobX > tx) painter.rect(tx + 1, ty + 1, knobX - tx - 1, 1, accent.bright());
		if (knobX > tx + 7) painter.rect(tx + 7, ty + 4, 1, 6, accent.lightest());
		if (knobX < tx + 18) {
			var ox = tx + 20;
			painter.rect(ox + 1, ty + 4, 3, 1, UiTheme.TEXT_2);
			painter.rect(ox + 1, ty + 9, 3, 1, UiTheme.TEXT_2);
			painter.rect(ox, ty + 5, 1, 4, UiTheme.TEXT_2);
			painter.rect(ox + 4, ty + 5, 1, 4, UiTheme.TEXT_2);
		}
		painter.bevel(
			new UiBounds(knobX, ty + TRACK_HEIGHT - 16, knobWidth, 14),
			UiTheme.WINDOW_EDGE,
			(hover.get() > 0.5 || down) && enabled ? UiTheme.KNOB_HI : UiTheme.KNOB,
			UiTheme.KNOB_HI,
			UiTheme.KNOB_HI,
			UiTheme.KNOB_LO,
			2,
			down
		);
	}
	@Override
	public boolean mouseClicked(UiContext ctx, double mouseX, double mouseY, int button) {
		if (button != 0 || !focusable() || !hovered(ctx)) return false;
		ctx.focus.set(this);
		down = true;
		return true;
	}
	@Override
	public boolean mouseReleased(UiContext ctx, double mouseX, double mouseY, int button) {
		if (!down) return false;
		down = false;
		if (button != 0 || !enabled || !bounds().contains(mouseX, mouseY)) return true;
		var next = !value.getAsBoolean();
		UiSound.click(next ? 1.15F : 0.95F);
		setter.accept(next);
		return true;
	}
	@Override
	public boolean keyPressed(UiContext ctx, int keyCode, int scanCode, int modifiers) {
		if (!focusable() || !ctx.focus.is(this) || keyCode != 257 && keyCode != 32) return false;
		var next = !value.getAsBoolean();
		UiSound.click(next ? 1.15F : 0.95F);
		setter.accept(next);
		return true;
	}
}
