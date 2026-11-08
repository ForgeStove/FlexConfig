package io.github.forgestove.flexconfig.foundation.ui.widget;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
public class UiScrollPane extends UiScrollView {
	protected static final int TRACK_WIDTH = 2, KNOB_WIDTH = 8, MIN_KNOB = 12, SHADOW_HEIGHT = 6, KNOB_SHADOW = 2;
	protected boolean draggingBar;
	protected int dragGrabY;
	@Override
	protected void paintOverlay(UiContext ctx) {
		paintShadows(ctx);
		paintBar(ctx);
	}
	protected void paintShadows(UiContext ctx) {
		if (!shown()) return;
		var b = bounds();
		var max = maxScroll();
		var top = (float) Math.min(1D, motion().value() / SHADOW_HEIGHT);
		var bottom = (float) Math.min(1D, (max - motion().value()) / SHADOW_HEIGHT);
		if (top > 0) ctx.painter.gradient(new UiBounds(b.x(), b.y(), b.width(), SHADOW_HEIGHT), UiTheme.SHADOW, 0x00000000);
		if (bottom > 0)
			ctx.painter.gradient(new UiBounds(b.x(), b.bottom() - SHADOW_HEIGHT, b.width(), SHADOW_HEIGHT), 0x00000000, UiTheme.SHADOW);
	}
	protected void paintBar(UiContext ctx) {
		if (!shown()) return;
		var bar = barBounds();
		var painter = ctx.painter;
		var trackX = bar.x() + (bar.width() - TRACK_WIDTH) / 2;
		painter.rect(trackX, bar.y(), TRACK_WIDTH, bar.height(), 0xFF58585A);
		var knobHeight = knobHeight();
		var knobY = knobY(knobHeight);
		var lit = draggingBar || bar.contains(ctx.mouseX, ctx.mouseY);
		painter.bevel(
			new UiBounds(bar.x(), knobY, bar.width(), knobHeight),
			0xFF000000,
			lit ? 0xFFF4F6F9 : 0xFFE6E8EB,
			lit ? 0xFFFAFBFC : 0xFFF5F5F7,
			lit ? 0xFFF8F9FB : 0xFFF0F1F3,
			0xFF58585A,
			3,
			false
		);
		var shadowY = knobY + knobHeight;
		if (shadowY < bar.bottom()) painter.rect(bar.x(), shadowY, bar.width(), Math.min(KNOB_SHADOW, bar.bottom() - shadowY), 0x33000000);
	}
	protected UiBounds barBounds() {
		var b = bounds();
		return new UiBounds(b.right() - KNOB_WIDTH, b.y(), KNOB_WIDTH, b.height());
	}
	protected int knobHeight() {
		var bar = barBounds();
		return Math.max(MIN_KNOB, bar.height() * bar.height() / Math.max(1, contentHeight()));
	}
	protected int knobY(int knobHeight) {
		var bar = barBounds();
		var max = maxScroll();
		return bar.y() + (max > 0 ? (int) Math.floor((bar.height() - knobHeight) * (motion().value() / max)) : 0);
	}
	@Override
	public boolean mouseClicked(UiContext ctx, double mouseX, double mouseY, int button) {
		var bar = barBounds();
		if (button != 0 || maxScroll() <= 0 || !bar.contains(mouseX, mouseY)) return false;
		var knobHeight = knobHeight();
		var knobY = knobY(knobHeight);
		if (mouseY >= knobY && mouseY < knobY + knobHeight) {
			draggingBar = true;
			dragGrabY = (int) (mouseY - knobY);
		} else scrollBy(mouseY < knobY ? -bounds().height() : bounds().height());
		return true;
	}
	@Override
	public boolean mouseDragged(UiContext ctx, double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (!draggingBar || button != 0) return false;
		setFromKnobTop(mouseY - dragGrabY);
		return true;
	}
	@Override
	public boolean mouseReleased(UiContext ctx, double mouseX, double mouseY, int button) {
		if (!draggingBar) return false;
		draggingBar = false;
		return true;
	}
	protected void setFromKnobTop(double knobTop) {
		var bar = barBounds();
		var knobHeight = knobHeight();
		var travel = bar.height() - knobHeight;
		var fraction = travel <= 0 ? 0 : (knobTop - bar.y()) / travel;
		motion().snap(clamp(fraction * maxScroll()));
	}
}
