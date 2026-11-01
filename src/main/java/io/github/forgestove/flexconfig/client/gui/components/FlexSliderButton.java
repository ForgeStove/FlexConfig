package io.github.forgestove.flexconfig.client.gui.components;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.*;
public final class FlexSliderButton extends AbstractSliderButton {
	private final double min, max;
	private final DoubleConsumer onDrag;
	private final Runnable onCommit;
	private final DoubleFunction<String> format;
	public FlexSliderButton(
		int width,
		int height,
		double min,
		double max,
		DoubleSupplier value,
		DoubleConsumer onDrag,
		Runnable onCommit,
		DoubleFunction<String> format
	) {
		super(0, 0, width, height, Component.empty(), progress(value.getAsDouble(), min, max));
		this.min = min;
		this.max = max;
		this.onDrag = onDrag;
		this.onCommit = onCommit;
		this.format = format;
		updateMessage();
	}
	private static double progress(double value, double min, double max) {
		var span = max - min;
		return span <= 0 ? 0 : Math.min(Math.max((value - min) / span, 0), 1);
	}
	/** 值被外部改动（重置/撤销）后同步进度条；只刷新显示，不回写配置。 */
	public void syncTo(double newValue) {
		value = progress(newValue, min, max);
		updateMessage();
	}
	@Override
	public void onRelease(double mouseX, double mouseY) {
		super.onRelease(mouseX, mouseY);
		onCommit.run();
	}
	@Override
	protected void updateMessage() {
		setMessage(Component.literal(format.apply(min + value * (max - min))));
	}
	@Override
	protected void applyValue() {
		onDrag.accept(min + value * (max - min));
	}
}
