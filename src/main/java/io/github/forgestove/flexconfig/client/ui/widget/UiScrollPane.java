package io.github.forgestove.flexconfig.client.ui.widget;
import io.github.forgestove.flexconfig.client.ui.core.*;
import net.minecraft.Util;
/** 滚动容器：裁剪内容、平滑滚动，右侧带常驻的立体滚动条。 */
public class UiScrollPane extends UiControl {
	private static final int TRACK_WIDTH = 2, KNOB_WIDTH = 8, MIN_KNOB = 12, SHADOW_HEIGHT = 6, KNOB_SHADOW = 2;
	private static final double[] WHEEL_ACCEL = {1, 1.2, 1.6, 2.2, 3};
	private final UiAnim offset = new UiAnim(0, UiMotion.SCROLL);
	private int contentHeight, pitch = 30;
	private long lastWheelMs = -1_000_000_000L;
	private int wheelRun;
	private boolean draggingBar;
	private int dragGrabY;
	protected void renderContent(UiContext ui) {}
	public void setPitch(int pitch) {
		this.pitch = Math.max(1, pitch);
	}
	public void setContentHeight(int height) {
		contentHeight = Math.max(0, height);
		var clamped = clamp(offset.target());
		if (clamped != offset.target()) offset.snap(clamped);
	}
	public int maxScroll() {
		return Math.max(0, contentHeight - bounds().height());
	}
	public boolean shown() {
		return maxScroll() > 0;
	}
	public double scroll() {
		return offset.get();
	}
	public void setScroll(double value) {
		offset.snap(clamp(value));
	}
	public void scrollTo(double target) {
		offset.target(clamp(target));
	}
	public void scrollBy(double delta) {
		scrollTo(offset.target() + delta);
	}
	public void wheel(double amount) {
		if (maxScroll() <= 0 || amount == 0) return;
		var now = Util.getMillis();
		wheelRun = now - lastWheelMs < 120 ? wheelRun + 1 : 0;
		lastWheelMs = now;
		var accel = WHEEL_ACCEL[Math.min(wheelRun, WHEEL_ACCEL.length - 1)];
		scrollBy(-amount * pitch * accel);
	}
	@Override
	public boolean mouseScrolled(UiContext ctx, double mouseX, double mouseY, double amount) {
		if (!bounds().contains(mouseX, mouseY) || maxScroll() <= 0) return false;
		wheel(amount);
		return true;
	}
	private double clamp(double value) {
		return UiMath.clamp(value, 0, maxScroll());
	}
	@Override
	public void render(UiContext ctx) {
		if (!visible) return;
		offset.update(ctx.delta);
		ctx.painter.scissorOn(bounds());
		renderContent(ctx);
		ctx.painter.scissorOff();
		paintShadows(ctx);
		paintBar(ctx);
	}
	private void paintShadows(UiContext ctx) {
		if (!shown()) return;
		var b = bounds();
		var max = maxScroll();
		var top = (float) Math.min(1D, offset.get() / SHADOW_HEIGHT);
		var bottom = (float) Math.min(1D, (max - offset.get()) / SHADOW_HEIGHT);
		if (top > 0) ctx.painter.gradient(new UiBounds(b.x(), b.y(), b.width(), SHADOW_HEIGHT), UiTheme.SHADOW, 0x00000000);
		if (bottom > 0)
			ctx.painter.gradient(new UiBounds(b.x(), b.bottom() - SHADOW_HEIGHT, b.width(), SHADOW_HEIGHT), 0x00000000, UiTheme.SHADOW);
	}
	private void paintBar(UiContext ctx) {
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
	private UiBounds barBounds() {
		var b = bounds();
		return new UiBounds(b.right() - KNOB_WIDTH, b.y(), KNOB_WIDTH, b.height());
	}
	private int knobHeight() {
		var bar = barBounds();
		return Math.max(MIN_KNOB, bar.height() * bar.height() / Math.max(1, contentHeight));
	}
	private int knobY(int knobHeight) {
		var bar = barBounds();
		var max = maxScroll();
		return bar.y() + (max > 0 ? (int) Math.floor((bar.height() - knobHeight) * (offset.get() / max)) : 0);
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
	private void setFromKnobTop(double knobTop) {
		var bar = barBounds();
		var knobHeight = knobHeight();
		var travel = bar.height() - knobHeight;
		var fraction = travel <= 0 ? 0 : (knobTop - bar.y()) / travel;
		offset.snap(clamp(fraction * maxScroll()));
	}
	@Override
	public boolean focusable() {
		return false;
	}
}
