package io.github.forgestove.flexconfig.client.gui.im.widget;
import io.github.forgestove.flexconfig.client.gui.im.core.*;
import net.minecraft.Util;
/** 自动淡出的竖直滚动条，滚轮加速、悬停加宽。 */
public class UiScrollbar {
	private static final int MIN_KNOB = 16;
	private static final double DECAY = 27.7;
	private static final long IDLE_MS = 800, WHEEL_WINDOW_MS = 120;
	private static final double[] WHEEL_ACCEL = {1, 1.2, 1.6, 2.2, 3};
	private final UiAnim hover = new UiAnim(0, UiMotion.CONTROL);
	private final UiTween visible = new UiTween(0);
	private UiBounds bounds = UiBounds.EMPTY;
	private double scroll, target;
	private boolean dragging;
	private double grab;
	private int contentHeight, viewportHeight;
	private long lastWake, lastWheel;
	private int wheelRun;
	public UiBounds bounds() {
		return bounds;
	}
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
		wake();
	}
	public void update(float delta, double mouseX, double mouseY) {
		var over = bounds.contains(mouseX, mouseY);
		hover.target(over || dragging ? 1 : 0);
		hover.update(delta);
		if (over || dragging) wake();
		var now = Util.getMillis();
		var wantVisible = shown() && (dragging || now - lastWake <= IDLE_MS);
		var targetVisible = wantVisible ? 1F : 0F;
		if (visible.target() != targetVisible) visible.start(visible.value(), targetVisible, UiMotion.TOOLTIP_FADE);
		visible.update(delta);
		target = UiMath.clamp(target, 0, maxScroll());
		scroll = UiMath.clamp(UiMath.smooth(scroll, target, DECAY, delta), 0, maxScroll());
		if (Math.abs(target - scroll) < 0.5) scroll = target;
	}
	public boolean shown() {
		return contentHeight > viewportHeight;
	}
	private void wake() {
		lastWake = Util.getMillis();
		if (visible.target() != 1F) visible.start(visible.value(), 1, UiMotion.TOOLTIP_FADE);
	}
	public void wheel(double amount, double step) {
		var now = Util.getMillis();
		wheelRun = now - lastWheel < WHEEL_WINDOW_MS ? wheelRun + 1 : 0;
		lastWheel = now;
		var accel = WHEEL_ACCEL[Math.min(wheelRun, WHEEL_ACCEL.length - 1)];
		target = UiMath.clamp(target - amount * step * accel, 0, maxScroll());
		wake();
	}
	public void render(UiContext ctx) {
		if (!shown() || visible.value() <= 0.01F) return;
		var width = hover.get() > 0.5 ? bounds.width() : Math.max(1, bounds.width() - 2);
		var x = bounds.right() - width;
		var knobHeight = knobHeight();
		var knobY = knobY(knobHeight);
		ctx.painter.pushAlpha(visible.value());
		ctx.painter.rect(x, knobY, width, knobHeight, hover.get() > 0.5 ? UiTheme.SCROLL_THUMB_HOVER : UiTheme.SCROLL_THUMB);
		ctx.painter.popAlpha();
	}
	@SuppressWarnings("unused")
	public boolean mouseClicked(UiContext ctx, double mouseX, double mouseY, int button) {
		if (button != 0 || !shown() || !bounds.contains(mouseX, mouseY)) return false;
		var knobHeight = knobHeight();
		var knobY = knobY(knobHeight);
		if (mouseY >= knobY && mouseY < knobY + knobHeight) {
			dragging = true;
			grab = mouseY - knobY;
		} else {
			grab = knobHeight / 2.0;
			target = scrollFor(mouseY);
		}
		wake();
		return true;
	}
	@SuppressWarnings("unused")
	public boolean mouseDragged(UiContext ctx, double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (!dragging || button != 0) return false;
		scroll = target = scrollFor(mouseY);
		wake();
		return true;
	}
	@SuppressWarnings("unused")
	public boolean mouseReleased(UiContext ctx, double mouseX, double mouseY, int button) {
		if (!dragging) return false;
		dragging = false;
		wake();
		return true;
	}
	private double maxScroll() {
		return Math.max(0, contentHeight - viewportHeight);
	}
	private int knobHeight() {
		return Math.max(MIN_KNOB, bounds.height() * bounds.height() / Math.max(1, contentHeight));
	}
	private int knobY(int knobHeight) {
		var max = maxScroll();
		return max <= 0 ? bounds.y() : bounds.y() + (int) Math.round((bounds.height() - knobHeight) * (scroll / max));
	}
	private double scrollFor(double mouseY) {
		var travel = bounds.height() - knobHeight();
		if (travel <= 0) return scroll;
		return UiMath.clamp((mouseY - grab - bounds.y()) * maxScroll() / travel, 0, maxScroll());
	}
}
