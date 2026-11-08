package io.github.forgestove.flexconfig.foundation.ui.widget;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import net.minecraft.util.Mth;

import java.util.function.*;
public class UiColorSlider extends UiControl {
	private static final int KNOB_W = 10, KNOB_H = 12;
	private final DoubleSupplier value;
	private final DoubleConsumer onChange;
	private final Track track;
	private final UiAnim hover = new UiAnim(0);
	private boolean dragging;
	public UiColorSlider(DoubleSupplier value, DoubleConsumer onChange, Track track) {
		this.value = value;
		this.onChange = onChange;
		this.track = track;
	}
	@Override
	public void render(UiContext ctx) {
		if (!visible) return;
		hover.target(focusable() && (hovered(ctx) || dragging) ? 1 : 0);
		hover.update(ctx.delta);
		var painter = ctx.painter;
		var trackBounds = trackBounds();
		painter.rect(trackBounds, UiTheme.WINDOW_EDGE);
		track.render(painter, trackBounds.inset(1, 1));
		var knobX = Math.round(knobCenter()) - KNOB_W / 2;
		var face = dragging ? 0xFFB1B2B5 : hover.get() > 0.5 ? 0xFFF4F6F9 : 0xFFD0D1D4;
		painter.bevel(
			new UiBounds(knobX, bounds().y() + (bounds().height() - KNOB_H) / 2 - 1, KNOB_W, KNOB_H),
			UiTheme.WINDOW_EDGE,
			face,
			0xFFECECED,
			0xFFE2E3E5,
			0xFF58585A,
			2,
			dragging
		);
	}
	private UiBounds trackBounds() {
		return new UiBounds(bounds().x(), bounds().y() + (bounds().height() - 6) / 2, bounds().width(), 6);
	}
	private float knobCenter() {
		return bounds().x() + KNOB_W / 2F + (float) Mth.clamp(value.getAsDouble(), 0D, 1D) * Math.max(1, bounds().width() - KNOB_W);
	}
	@Override
	public boolean mouseClicked(UiContext ctx, double mouseX, double mouseY, int button) {
		if (button != 0 || !focusable() || !hovered(ctx)) return false;
		ctx.focus.set(this);
		dragging = true;
		ctx.setDragOwner(this);
		apply(mouseX);
		return true;
	}
	@Override
	public boolean mouseDragged(UiContext ctx, double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (!dragging || button != 0) return false;
		apply(mouseX);
		return true;
	}
	@Override
	public boolean mouseReleased(UiContext ctx, double mouseX, double mouseY, int button) {
		if (!dragging) return false;
		dragging = false;
		return true;
	}
	@Override
	public boolean keyPressed(UiContext ctx, int keyCode, int scanCode, int modifiers) {
		if (!focusable() || !ctx.focus.is(this)) return false;
		var delta = switch (keyCode) {
			case 263, 264 -> -0.01;   // left, down
			case 262, 265 -> 0.01;    // right, up
			default -> 0D;
		};
		if (delta == 0) return false;
		onChange.accept(Mth.clamp(value.getAsDouble() + delta, 0D, 1D));
		return true;
	}
	private void apply(double mouseX) {
		var ratio = Mth.clamp((float) ((mouseX - bounds().x() - KNOB_W / 2F) / Math.max(1, bounds().width() - KNOB_W)), 0F, 1F);
		onChange.accept(ratio);
	}
	/** 轨道内容，调用方按通道绘制渐变；范围已去掉 1px 描边。 */
	public interface Track {
		void render(UiPainter painter, UiBounds bounds);
	}
}
