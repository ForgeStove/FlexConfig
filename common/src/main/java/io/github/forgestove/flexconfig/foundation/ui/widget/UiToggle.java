package io.github.forgestove.flexconfig.foundation.ui.widget;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import net.minecraft.util.Mth;

import java.util.function.*;
public class UiToggle extends UiControl {
	private static final int TRACK_WIDTH = 30, TRACK_HEIGHT = 14, KNOB = 16;
	private final UiAnim knob = new UiAnim(0, UiMotion.CONTROL);
	private final UiAnim hover = new UiAnim(0);
	private final BooleanSupplier value;
	private final Consumer<Boolean> setter;
	public UiToggle(BooleanSupplier value, Consumer<Boolean> setter) {
		this.value = value;
		this.setter = setter;
		knob.jumpTo(value.getAsBoolean() ? TRACK_WIDTH - KNOB : 0);
	}
	@Override
	public void render(UiContext ctx) {
		if (!visible) return;
		hover.target(focusable() && (hovered(ctx) || down) ? 1 : 0);
		hover.update(ctx.delta);
		knob.target(value.getAsBoolean() ? TRACK_WIDTH - KNOB : 0);
		knob.update(ctx.delta);
		var painter = ctx.painter;
		var accent = ctx.accent();
		var tx = bounds().x() + (bounds().width() - TRACK_WIDTH) / 2;
		var ty = bounds().y() + bounds().height() - TRACK_HEIGHT;
		var track = new UiBounds(tx, ty, TRACK_WIDTH, TRACK_HEIGHT);
		var knobX = tx + (int) Math.round(knob.get());
		var knobWidth = KNOB;
		var outline = enabled ? UiTheme.WINDOW_EDGE : 0xFF8C8D90;
		var onFill = enabled ? accent.base() : 0xFFD0D1D4;
		var offFill = enabled ? 0xFF8C8D90 : 0xFFD0D1D4;
		double value1 = knob.get() / (TRACK_WIDTH - KNOB);
		var onShare = Mth.clamp(value1, 0D, 1D);
		painter.rect(track, outline);
		painter.rect(tx + 1, ty + 1, Math.max(0, knobX - tx - 1), TRACK_HEIGHT - 2, onFill);
		painter.rect(knobX + knobWidth, ty + 1, Math.max(0, tx + TRACK_WIDTH - 1 - knobX - knobWidth), TRACK_HEIGHT - 2, offFill);
		if (knobX > tx) painter.rect(
			tx + 1,
			ty + 1,
			knobX - tx - 1,
			1,
			enabled ? UiTheme.fadeToward(accent.light(), onFill, (float) onShare) : offFill
		);
		if (knobX > tx + 7) painter.rect(tx + 7, ty + 4, 1, 6, enabled ? 0xFFFFFFFF : 0xFF6D6D6D);
		if (knobX < tx + 18) {
			var ox = tx + 19;
			var mark = enabled ? 0xFF242425 : 0xFF666666;
			painter.rect(ox + 1, ty + 4, 4, 1, mark);
			painter.rect(ox + 1, ty + 9, 4, 1, mark);
			painter.rect(ox, ty + 5, 1, 4, mark);
			painter.rect(ox + 5, ty + 5, 1, 4, mark);
		}
		var knobFace = !enabled ? 0xFFD0D1D4 : down ? 0xFFB1B2B5 : hover.get() > 0.5 ? 0xFFF4F6F9 : 0xFFD0D1D4;
		painter.bevel(
			new UiBounds(knobX, ty + TRACK_HEIGHT - KNOB, knobWidth, KNOB),
			outline,
			knobFace,
			0xFFECECED,
			0xFFE2E3E5,
			enabled ? 0xFF58585A : 0xFFB1B2B5,
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
