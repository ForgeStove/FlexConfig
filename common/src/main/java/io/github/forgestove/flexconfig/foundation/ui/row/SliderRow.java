package io.github.forgestove.flexconfig.foundation.ui.row;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.model.RowContext;
import io.github.forgestove.flexconfig.foundation.ui.widget.*;
import io.github.forgestove.flexconfig.foundation.tree.ValueConfigNode;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.DoubleFunction;
/** 滑条行：轨道横跨整行，数值显示在右端。 */
public class SliderRow extends ValueRow {
	private final UiSlider slider;
	private final DoubleFunction<Object> converter;
	public SliderRow(RowContext ctx, ValueConfigNode<?, ?> node, DoubleFunction<Object> converter) {
		super(ctx, node);
		this.converter = converter;
		var range = Objects.requireNonNull(node.getSliderRange(), "slider row needs a slider range");
		slider = new UiSlider(
			range.min(),
			range.max(),
			range.stepOrAuto(),
			() -> ((Number) value()).doubleValue(),
			value -> writeValue(converter.apply(value)),
			ctx::refresh,
			this::formatValue
		);
		var fallback = node.getDefaultValue();
		if (fallback instanceof Number number) slider.defaultValue(number::doubleValue);
	}
	private String formatValue(double raw) {
		var converted = converter.apply(raw);
		if (converted instanceof Float value) return new BigDecimal(Float.toString(value)).stripTrailingZeros().toPlainString();
		if (converted instanceof Double value) return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
		return String.valueOf(converted);
	}
	@Override
	protected String defaultText() {
		var value = node.getDefaultValue();
		return value instanceof Number number ? formatValue(number.doubleValue()) : super.defaultText();
	}
	@Override
	public int heightFor(int width) {
		var base = super.heightFor(width);
		var lines = descriptionLineCount(width);
		return lines == 0 ? base : base + 12 + UiTheme.GAP;
	}
	@Override
	protected int titleTop() {
		return bounds().y() + 5;
	}
	@Override
	protected int titleRightInset() {
		return 40 + UiTheme.GAP + UiTheme.ACTION_SIZE * 2 + UiTheme.GAP;
	}
	@Override
	public void layout() {
		super.layout();
		var left = bounds().x() + UiTheme.INDENT + depth * UiTheme.INDENT + 4;
		var right = bounds().right() - UiTheme.PADDING - 4;
		var height = UiTheme.CONTROL_HEIGHT;
		slider.setBounds(new UiBounds(left, bounds().bottom() - height - 4, Math.max(60, right - left), height));
		slider.setValueCell(new UiBounds(bounds().right() - UiTheme.PADDING - 40, titleTop(), 40, 9));
		buttons.layout(bounds().right() - UiTheme.PADDING - 40 - UiTheme.GAP, titleTop() - 3, UiTheme.CONTROL_HEIGHT);
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
