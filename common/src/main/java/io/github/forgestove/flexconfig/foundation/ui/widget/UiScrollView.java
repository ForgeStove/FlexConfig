package io.github.forgestove.flexconfig.foundation.ui.widget;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import net.minecraft.Util;
import net.minecraft.util.Mth;

import java.util.Objects;
public class UiScrollView extends UiControl {
	protected static final double[] WHEEL_ACCEL = {1, 1.2, 1.6, 2.2, 3};
	private ScrollMotion motion = new SpringScrollMotion(UiMotion.SCROLL);
	private int contentHeight, pitch = 30;
	private long lastWheelMs = -1_000_000_000L;
	private int wheelRun;
	public ScrollMotion motion() {
		return motion;
	}
	public void setMotion(ScrollMotion motion) {
		this.motion = Objects.requireNonNull(motion);
	}
	public void setPitch(int pitch) {
		this.pitch = Math.max(1, pitch);
	}
	public void setContentHeight(int height) {
		contentHeight = Math.max(0, height);
		var clamped = clamp(motion.target());
		if (clamped != motion.target()) motion.snap(clamped);
	}
	public int contentHeight() {
		return contentHeight;
	}
	public int maxScroll() {
		return Math.max(0, contentHeight - bounds().height());
	}
	public boolean shown() {
		return maxScroll() > 0;
	}
	public double scroll() {
		return motion.value();
	}
	public void setScroll(double value) {
		motion.snap(clamp(value));
	}
	public void scrollTo(double target) {
		motion.setTarget(clamp(target));
	}
	public void scrollBy(double delta) {
		scrollTo(motion.target() + delta);
	}
	public void wheel(double amount) {
		if (maxScroll() <= 0 || amount == 0) return;
		var now = Util.getMillis();
		wheelRun = now - lastWheelMs < 120 ? wheelRun + 1 : 0;
		lastWheelMs = now;
		var accel = WHEEL_ACCEL[Math.min(wheelRun, WHEEL_ACCEL.length - 1)];
		scrollBy(-amount * pitch * accel);
	}
	protected double clamp(double value) {
		return Mth.clamp(value, 0, maxScroll());
	}
	/** 绘制滚动内容；调用时裁剪已按 {@link #bounds()} 开启。 */
	protected void renderContent(UiContext ui) {}
	/** 内容之上、不受裁剪影响的装饰层（滚动条、边缘阴影等），默认不画。 */
	protected void paintOverlay(UiContext ui) {}
	@Override
	public boolean focusable() {
		return false;
	}
	@Override
	public void render(UiContext ctx) {
		if (!visible) return;
		motion.update(ctx.delta);
		ctx.painter.scissorOn(bounds());
		renderContent(ctx);
		ctx.painter.scissorOff();
		paintOverlay(ctx);
	}
	@Override
	public boolean mouseScrolled(UiContext ctx, double mouseX, double mouseY, double amount) {
		if (!bounds().contains(mouseX, mouseY) || maxScroll() <= 0) return false;
		wheel(amount);
		return true;
	}
}
