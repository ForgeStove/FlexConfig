package io.github.forgestove.flexconfig.client.gui.components;
import io.github.forgestove.flexconfig.client.gui.FlexGuiTextures;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
public class OreSwitchButton extends OreAbstractButton {
	/** 滑动时长与缓动曲线，对应 125ms cubic-bezier(.34, 1.56, .64, 1)。 */
	private static final float SLIDE_MS = 125f;
	private static final float CURVE_X1 = 0.34f, CURVE_Y1 = 1.56f, CURVE_X2 = 0.64f;
	private static final int RING_W = 1, RING_COLOR = 0xFFFFFFFF;
	private final Consumer<Boolean> onToggle;
	private boolean value;
	private boolean sliding;
	private long slideStart;
	private float slideFrom;
	/** 滑钮位置：0 = 关，1 = 开，中间为动画值。 */
	private float progress;
	public OreSwitchButton(int width, int height, Component message, boolean initialValue, Consumer<Boolean> onToggle) {
		super(0, 0, width, height, message);
		value = initialValue;
		progress = initialValue ? 1f : 0f;
		this.onToggle = onToggle;
	}
	/** 控件宽 = 轨道宽，显示与交互范围一致。 */
	public static int preferredWidth() {
		return FlexGuiTextures.TOGGLE_TRACK.width();
	}
	/** 求解 cubic-bezier(.34, 1.56, .64, 1)：先用二分法由 x 反解参数 t，再取其 y。 */
	private static float ease(float x) {
		var lo = 0f;
		var hi = 1f;
		for (var i = 0; i < 16; i++) {
			var mid = (lo + hi) / 2;
			if (bezier(CURVE_X1, CURVE_X2, mid) < x) lo = mid;
			else hi = mid;
		}
		return bezier(CURVE_Y1, 1f, (lo + hi) / 2);
	}
	private static float bezier(float p1, float p2, float t) {
		var u = 1 - t;
		return 3 * u * u * t * p1 + 3 * u * t * t * p2 + t * t * t;
	}
	/** 外部同步值，不触发回调。 */
	public void setValue(boolean newValue) {
		if (value == newValue) return;
		value = newValue;
		startSlide();
	}
	private void startSlide() {
		slideFrom = progress;
		slideStart = Util.getMillis();
		sliding = true;
	}
	@Override
	public void onPress() {
		value = !value;
		startSlide();
		onToggle.accept(value);
	}
	@Override
	protected void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		stepSlide();
		// 尺寸取自纹理表（atlas region），每帧读取，资源包改 region 即生效
		var trackW = FlexGuiTextures.TOGGLE_TRACK.width();
		var trackH = FlexGuiTextures.TOGGLE_TRACK.height();
		var thumbSize = FlexGuiTextures.THUMB.width();
		var trackX = getX() + (width - trackW) / 2;
		// 轨道底边与滑钮底边齐平，钮只在顶部高出
		var trackY = getY() + thumbSize - trackH;
		renderTrack(gui, trackX, trackY);
		// 行程 = 轨道宽 − 钮宽：关态钮左缘、开态钮右缘都与轨道边缘齐平
		var thumbX = trackX + Math.round((trackW - thumbSize) * progress);
		renderThumb(gui, thumbX, getY());
		if (shouldRenderFocus()) renderRing(gui, thumbX, getY(), thumbSize);
		renderCursor();
	}
	private void stepSlide() {
		var target = value ? 1f : 0f;
		if (!sliding) {
			progress = target;
			return;
		}
		var t = (Util.getMillis() - slideStart) / SLIDE_MS;
		if (t >= 1f) {
			progress = target;
			sliding = false;
			return;
		}
		progress = slideFrom + (target - slideFrom) * ease(t);
	}
	/** 轨道是一张常驻的两段式贴图，滑钮遮住哪半就显出另半。 */
	private void renderTrack(@NotNull GuiGraphics gui, int x, int y) {
		(active ? FlexGuiTextures.TOGGLE_TRACK : FlexGuiTextures.TOGGLE_TRACK_DISABLED).render(gui, x, y);
	}
	private void renderThumb(@NotNull GuiGraphics gui, int x, int y) {
		var texture = !active
			? FlexGuiTextures.THUMB_DISABLED
			: isPressed() ? FlexGuiTextures.THUMB_PRESSED : isHovered() ? FlexGuiTextures.THUMB_HOVER : FlexGuiTextures.THUMB;
		texture.render(gui, x, y);
	}
	private void renderRing(@NotNull GuiGraphics gui, int x, int y, int size) {
		gui.renderOutline(x - RING_W, y - RING_W, size + 2 * RING_W, size + 2 * RING_W, RING_COLOR);
	}
}
