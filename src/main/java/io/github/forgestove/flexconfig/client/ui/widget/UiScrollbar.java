package io.github.forgestove.flexconfig.client.ui.widget;
import io.github.forgestove.flexconfig.client.ui.core.*;
import net.minecraft.Util;
public class UiScrollbar {
	private static final int MIN_KNOB = 12, SHADOW_H = 2;
	private static final int TRACK_WIDTH = 2;
	private static final double DECAY = 27.7;
	private static final long WHEEL_WINDOW_MS = 120;
	private static final double[] WHEEL_ACCEL = {1, 1.2, 1.6, 2.2, 3};
	private UiBounds bounds = UiBounds.EMPTY;
	private double scroll, target;
	private boolean dragging;
	private double grab;
	private int contentHeight, viewportHeight;
	private long lastWheel;
	private int wheelRun;
	public void setBounds(UiBounds bounds) {
		this.bounds = bounds;
	}
	public void metrics(int contentHeight, int viewportHeight) {
		this.contentHeight = contentHeight;
		this.viewportHeight = viewportHeight;
	}
	public double scroll() {
		return scroll;
	}
	public void setScroll(double scroll) {
		this.scroll = target = UiMath.clamp(scroll, 0, maxScroll());
	}
	@SuppressWarnings("unused")
	public void update(float delta, double mouseX, double mouseY) {
		target = UiMath.clamp(target, 0, maxScroll());
		scroll = UiMath.clamp(UiMath.smooth(scroll, target, DECAY, delta), 0, maxScroll());
		if (Math.abs(target - scroll) < 0.5) scroll = target;
	}
	public boolean shown() {
		return contentHeight > viewportHeight;
	}
	public void wheel(double amount, double step) {
		var now = Util.getMillis();
		wheelRun = now - lastWheel < WHEEL_WINDOW_MS ? wheelRun + 1 : 0;
		lastWheel = now;
		var accel = WHEEL_ACCEL[Math.min(wheelRun, WHEEL_ACCEL.length - 1)];
		target = UiMath.clamp(target - amount * step * accel, 0, maxScroll());
	}
	public void render(UiContext ctx) {
		if (!shown()) return;
		var painter = ctx.painter;
		var trackX = bounds.x() + (bounds.width() - TRACK_WIDTH) / 2;
		painter.rect(trackX, bounds.y(), TRACK_WIDTH, bounds.height(), 0xFF58585A);
		var knobHeight = knobHeight();
		var knobY = (int) Math.floor(knobYExact(knobHeight));
		var lit = dragging || new UiBounds(bounds.x(), bounds.y(), bounds.width(), bounds.height()).contains(ctx.mouseX, ctx.mouseY);
		painter.bevel(
			new UiBounds(bounds.x(), knobY, bounds.width(), knobHeight),
			0xFF000000,
			lit ? 0xFFF4F6F9 : 0xFFE6E8EB,
			lit ? 0xFFFAFBFC : 0xFFF5F5F7,
			lit ? 0xFFF8F9FB : 0xFFF0F1F3,
			0xFF58585A,
			3,
			false
		);
		var shadowY = knobY + knobHeight;
		if (shadowY < bounds.bottom()) painter.rect(bounds.x(), shadowY, bounds.width(), Math.min(SHADOW_H, bounds.bottom() - shadowY), 0x33000000);
		if (scroll > 0) painter.gradient(new UiBounds(bounds.x(), bounds.y(), bounds.width(), 6), UiTheme.SHADOW, 0x00000000);
		if (scroll < maxScroll())
			painter.gradient(new UiBounds(bounds.x(), bounds.bottom() - 6, bounds.width(), 6), 0x00000000, UiTheme.SHADOW);
	}
	@SuppressWarnings("unused")
	public boolean mouseClicked(UiContext ctx, double mouseX, double mouseY, int button) {
		var hit = new UiBounds(bounds.x(), bounds.y(), bounds.width(), bounds.height());
		if (button != 0 || !shown() || !hit.contains(mouseX, mouseY)) return false;
		var knobHeight = knobHeight();
		var knobY = knobYExact(knobHeight);
		if (mouseY >= knobY && mouseY < knobY + knobHeight) {
			dragging = true;
			grab = mouseY - knobY;
		} else target = UiMath.clamp(target + (mouseY < knobY ? -viewportHeight : viewportHeight), 0, maxScroll());
		return true;
	}
	@SuppressWarnings("unused")
	public boolean mouseDragged(UiContext ctx, double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (!dragging || button != 0) return false;
		var travel = bounds.height() - knobHeight();
		if (travel > 0) scroll = target = UiMath.clamp((mouseY - grab - bounds.y()) * maxScroll() / travel, 0, maxScroll());
		return true;
	}
	@SuppressWarnings("unused")
	public boolean mouseReleased(UiContext ctx, double mouseX, double mouseY, int button) {
		if (!dragging) return false;
		dragging = false;
		return true;
	}
	private double maxScroll() {
		return Math.max(0, contentHeight - viewportHeight);
	}
	private int knobHeight() {
		return Math.max(MIN_KNOB, bounds.height() * bounds.height() / Math.max(1, contentHeight));
	}
	private double knobYExact(int knobHeight) {
		var max = maxScroll();
		return max > 0 ? bounds.y() + (bounds.height() - knobHeight) * (scroll / max) : bounds.y();
	}
}
