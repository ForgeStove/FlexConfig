package io.github.forgestove.flexconfig.client.ui.row;
import io.github.forgestove.flexconfig.client.ui.core.*;
import io.github.forgestove.flexconfig.client.ui.model.RowContext;
import io.github.forgestove.flexconfig.client.ui.widget.*;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;

import java.util.*;
import java.util.function.DoubleFunction;
/** 滑条行：轨道横跨整行，数值显示在右端。 */
public class SliderRow extends ValueRow {
	private final UiSlider slider;
	public SliderRow(RowContext ctx, ValueConfigNode<?, ?> node, DoubleFunction<Object> converter) {
		super(ctx, node);
		var range = Objects.requireNonNull(node.getSliderRange(), "slider row needs a slider range");
		slider = new UiSlider(
			range.min(),
			range.max(),
			() -> ((Number) value()).doubleValue(),
			value -> writeValue(converter.apply(value)),
			ctx::refresh,
			value -> String.valueOf(converter.apply(value))
		);
	}
	@Override
	protected boolean showDescription() {
		return false;
	}
	@Override
	protected int titleTop() {
		return bounds().y() + 5;
	}
	@Override
	public void layout() {
		super.layout();
		var left = bounds().x() + UiTheme.INDENT + depth * UiTheme.INDENT + 4;
		var right = bounds().right() - UiTheme.PADDING - actionBar.usedWidth() - 4;
		var height = Math.min(bounds().height(), UiTheme.CONTROL_HEIGHT);
		slider.setBounds(new UiBounds(left, bounds().bottom() - height - 4, Math.max(60, right - left), height));
	}
	@Override
	public void render(UiContext ui) {
		super.render(ui);
		slider.render(ui);
	}
	@Override
	public List<UiControl> controls() {
		var controls = new ArrayList<>(super.controls());
		controls.add(slider);
		var editor = slider.editor();
		if (editor != null) controls.add(editor);
		return controls;
	}
}
