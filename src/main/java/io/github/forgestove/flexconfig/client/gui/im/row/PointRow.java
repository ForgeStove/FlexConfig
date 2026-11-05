package io.github.forgestove.flexconfig.client.gui.im.row;
import io.github.forgestove.flexconfig.client.gui.im.core.UiTheme;
import io.github.forgestove.flexconfig.client.gui.im.model.RowContext;
import io.github.forgestove.flexconfig.client.gui.im.widget.UiTextField;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import java.awt.Point;
import java.util.regex.Pattern;
public class PointRow extends ValueRow {
	private static final Pattern INTEGER = Pattern.compile("-?\\d*");
	private final UiTextField xField, yField;
	public PointRow(RowContext ctx, ValueConfigNode<?, ?> node) {
		super(ctx, node);
		var width = (UiTheme.CONTROL_WIDTH - UiTheme.GAP) / 2;
		xField = new UiTextField(String.valueOf(point().x));
		xField.setFilter(s -> INTEGER.matcher(s).matches());
		xField.setResponder(this::onX);
		yField = new UiTextField(String.valueOf(point().y));
		yField.setFilter(s -> INTEGER.matcher(s).matches());
		yField.setResponder(this::onY);
		actionBar.add(yField, width).add(xField, width);
	}
	private Point point() {
		return (Point) value();
	}
	private void onX(String text) {
		try {
			setValue(new Point(isZero(text) ? 0 : Integer.parseInt(text), point().y));
		} catch (NumberFormatException ignored) {}
	}
	private void onY(String text) {
		try {
			setValue(new Point(point().x, isZero(text) ? 0 : Integer.parseInt(text)));
		} catch (NumberFormatException ignored) {}
	}
	private static boolean isZero(String text) {
		return text.isEmpty() || text.equals("-");
	}
	@Override
	public void refresh() {
		super.refresh();
		xField.setError(state.validationError != null);
		yField.setError(state.validationError != null);
		sync(xField, point().x);
		sync(yField, point().y);
	}
	private void sync(UiTextField field, int expected) {
		var text = field.value();
		try {
			if ((isZero(text) ? 0 : Integer.parseInt(text)) == expected) return;
		} catch (NumberFormatException ignored) {}
		field.setValue(String.valueOf(expected));
	}
	@Override
	public void resetToDefault() {
		super.resetToDefault();
		xField.setValue(String.valueOf(point().x));
		yField.setValue(String.valueOf(point().y));
	}
	@Override
	public void resetToActive() {
		super.resetToActive();
		xField.setValue(String.valueOf(point().x));
		yField.setValue(String.valueOf(point().y));
	}
}
