package io.github.forgestove.flexconfig.client.gui.components;
import io.github.forgestove.flexconfig.client.gui.FlexGuiTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.*;
/**
 * 行程与开关同款：滑块左缘在「轨道左 .. 轨道右 − 滑块宽」之间移动，两端贴齐。拖动按每帧鼠标坐标推进
 * （{@link #dragTo}），不走 {@code mouseDragged}。
 */
public final class OreSlider extends OreAbstractButton {
	private static final int RING = 0xFFFFFFFF;
	private final double min, max;
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
		DoubleSupplier value,
		DoubleConsumer onDrag,
		Runnable onCommit,
		DoubleFunction<String> format
	) {
		super(0, 0, width, height, Component.empty());
		this.min = min;
		this.max = max;
		this.value = value;
		this.onDrag = onDrag;
		this.onCommit = onCommit;
		this.format = format;
	}
	/** 触发时机由 {@link #mouseReleased} 接管：拖动须连续生效，不能等松手才动。 */
	@Override
	public void onPress() {}
	@Override
	protected void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		var handleSize = handleSize();
		var trackH = FlexGuiTextures.SLIDER_TRACK.height();
		var trackY = getY() + (getHeight() - trackH) / 2;
		var left = getX();
		var handleX = left + (int) Math.round(progress() * (getWidth() - handleSize));
		FlexGuiTextures.SLIDER_TRACK.render(gui, left, trackY, getWidth(), trackH);
		// 已填充段：同一张轨道图铺到滑块左缘；向右多铺 2px 延入滑块下方，避免绿段尾部留下一道描边
		if (active && handleX > left)
			FlexGuiTextures.SLIDER_TRACK_FILLED.render(gui, left, trackY, Math.min(handleX - left + 2, getWidth()), trackH);
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
	public void dragTo(double mouseX) {
		if (!dragging) return;
		var travel = getWidth() - handleSize();
		if (travel <= 0) return;
		var progress = Math.clamp((mouseX - getX() - handleSize() / 2.0) / travel, 0, 1);
		onDrag.accept(min + progress * (max - min));
	}
	@Override
	protected boolean isPressed() {
		return dragging;
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
	private int handleSize() {
		return FlexGuiTextures.THUMB.width();
	}
	private double progress() {
		var span = max - min;
		return span <= 0 ? 0 : Math.clamp((value.getAsDouble() - min) / span, 0, 1);
	}
}
