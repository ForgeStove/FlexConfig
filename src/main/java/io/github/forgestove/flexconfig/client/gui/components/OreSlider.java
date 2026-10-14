package io.github.forgestove.flexconfig.client.gui.components;
import io.github.forgestove.flexconfig.client.gui.FlexGuiTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;
/**
 * 滑块：分段轨道 + 已填充段 + 方形滑块，当前值画在轨道上。
 * <p>
 * 行程与开关同款：滑块左缘在「轨道左 .. 轨道右 − 滑块宽」之间移动，两端贴齐。
 * 拖动按每帧鼠标坐标推进（{@link #dragTo}），不走 {@code mouseDragged}。
 */
public final class OreSlider extends OreAbstractButton {
	private static final int RING = 0xFFFFFFFF, NOTCH = 0xFF1E1E1F;
	/** 格宽小于这个值就整段不画分段暗线，免得密成一片。 */
	private static final int MIN_CELL = 3;
	private final double min, max, step;
	private final DoubleSupplier value;
	private final DoubleConsumer onDrag;
	private final Runnable onCommit;
	private final DoubleFunction<String> format;
	private boolean dragging;
	public OreSlider(
		int width,
		int height,
		double min,
		double max,
		double step,
		DoubleSupplier value,
		DoubleConsumer onDrag,
		Runnable onCommit,
		DoubleFunction<String> format
	) {
		super(0, 0, width, height, Component.empty());
		this.min = min;
		this.max = max;
		this.step = step;
		this.value = value;
		this.onDrag = onDrag;
		this.onCommit = onCommit;
		this.format = format;
	}
	/** 触发时机由 {@link #mouseReleased} 自己管（拖动要连续跟手，不能等松手才动）。 */
	@Override
	public void onPress() {}
	/** 拖动落位；每帧喂当前鼠标坐标即可。 */
	public void dragTo(double mouseX) {
		if (!dragging) return;
		var travel = getWidth() - handleSize();
		if (travel <= 0) return;
		var progress = Math.clamp((mouseX - getX() - handleSize() / 2.0) / travel, 0, 1);
		onDrag.accept(min + progress * (max - min));
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!active || !visible || !isValidClickButton(button) || !isMouseOver(mouseX, mouseY)) return false;
		dragging = true;
		dragTo(mouseX);
		return true;
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (!dragging) return super.mouseReleased(mouseX, mouseY, button);
		dragging = false;
		onCommit.run();
		return true;
	}
	@Override
	protected boolean isPressed() {
		return dragging;
	}
	@Override
	protected void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		var handleSize = handleSize();
		var trackH = FlexGuiTextures.SLIDER_TRACK.height();
		var trackY = getY() + (getHeight() - trackH) / 2;
		var left = getX();
		var handleX = left + (int) Math.round(progress() * (getWidth() - handleSize));
		FlexGuiTextures.SLIDER_TRACK.render(gui, left, trackY, getWidth(), trackH);
		// 已填充段：同一张轨道图铺到滑块左缘；多铺 2px 塞到滑块底下，免得绿段尾巴上留一道描边
		if (active && handleX > left) FlexGuiTextures.SLIDER_TRACK_FILLED.render(
			gui,
			left,
			trackY,
			Math.min(handleX - left + 2, getWidth()),
			trackH
		);
		// 暗线：一格 = 一个步长，把轨道按步数均分；步长为 0（连续值）时不画
		var steps = step > 0 ? (int) Math.round((max - min) / step) : 0;
		if (steps > 1 && getWidth() / steps >= MIN_CELL) for (var i = 1; i < steps; i++) {
			var x = left + Math.round((float) i * getWidth() / steps);
			gui.fill(x, trackY, x + 1, trackY + trackH, NOTCH);
		}
		var handleY = getY() + (getHeight() - handleSize) / 2;
		var handle = active ? FlexGuiTextures.THUMB : FlexGuiTextures.THUMB_DISABLED;
		handle.render(gui, handleX, handleY);
		if (shouldRenderFocus()) gui.renderOutline(handleX - 1, handleY - 1, handleSize + 2, handleSize + 2, RING);
		gui.drawCenteredString(
			Minecraft.getInstance().font,
			format.apply(value.getAsDouble()),
			getX() + getWidth() / 2,
			getY() + (getHeight() - 8) / 2,
			0xFFFFFFFF
		);
		renderCursor();
	}
	/** 滑块边长 = 精灵边长（随资源包变）。 */
	private int handleSize() {
		return FlexGuiTextures.THUMB.width();
	}
	private double progress() {
		var span = max - min;
		return span <= 0 ? 0 : Math.clamp((value.getAsDouble() - min) / span, 0, 1);
	}
}
