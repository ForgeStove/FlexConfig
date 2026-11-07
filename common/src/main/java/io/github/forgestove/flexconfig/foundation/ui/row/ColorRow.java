package io.github.forgestove.flexconfig.foundation.ui.row;
import io.github.forgestove.flexconfig.foundation.client.*;
import io.github.forgestove.flexconfig.foundation.ui.ColorPickerScreen;
import io.github.forgestove.flexconfig.foundation.ui.core.UiTheme;
import io.github.forgestove.flexconfig.foundation.ui.model.RowContext;
import io.github.forgestove.flexconfig.foundation.ui.widget.*;
import io.github.forgestove.flexconfig.foundation.tree.ValueConfigNode;

import java.util.regex.Pattern;
public class ColorRow extends ValueRow {
	private static final Pattern HEX = Pattern.compile("[0-9A-Fa-f]*");
	private final UiTextField field;
	private final boolean alpha;
	public ColorRow(RowContext ctx, ValueConfigNode node) {
		super(ctx, node);
		alpha = node.colorHasAlpha();
		field = new UiTextField(format(colorValue()));
		field.setMaxLength(maxLength());
		field.setFilter(s -> HEX.matcher(s).matches());
		field.setResponder(this::onInput);
		var swatch = new UiColorSwatch(alpha, this::colorValue, this::openPicker);
		swatch.tooltip(Translation.COLOR_PICKER_TOOLTIP);
		actionBar.add(field, UiTheme.CONTROL_WIDTH - UiTheme.ACTION_SIZE - UiTheme.GAP).add(swatch, UiTheme.ACTION_SIZE);
	}
	private int colorValue() {
		return ((Number) value()).intValue();
	}
	private int maxLength() {
		return alpha ? 8 : 6;
	}
	private String format(int color) {
		return String.format(alpha ? "%08X" : "%06X", color);
	}
	private void onInput(String text) {
		if (text.length() != maxLength()) return;
		try {
			setValue(Integer.parseUnsignedInt(text, 16));
		} catch (NumberFormatException ignored) {}
	}
	private void openPicker() {
		ClientUtil.mc.setScreen(new ColorPickerScreen(ClientUtil.mc.screen, colorValue(), alpha, this::accept));
	}
	private void accept(int color) {
		setValue(color);
		field.setValue(format(color));
	}
	@Override
	protected String defaultText() {
		var value = node.getDefaultValue();
		return value instanceof Number number ? format(number.intValue()) : super.defaultText();
	}
	@Override
	public void refresh() {
		super.refresh();
		var valid = state.validationError == null && field.value().length() == maxLength();
		field.setError(!valid);
		if (valid && !field.value().equalsIgnoreCase(format(colorValue()))) field.setValue(format(colorValue()));
	}
	@Override
	public void resetToDefault() {
		super.resetToDefault();
		field.setValue(format(colorValue()));
	}
	@Override
	public void resetToActive() {
		super.resetToActive();
		field.setValue(format(colorValue()));
	}
}
