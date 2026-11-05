package io.github.forgestove.flexconfig.client.gui.im.row;
import io.github.forgestove.flexconfig.client.gui.im.core.UiTheme;
import io.github.forgestove.flexconfig.client.gui.im.model.RowContext;
import io.github.forgestove.flexconfig.client.gui.im.widget.UiTextField;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import java.util.function.Function;
import java.util.function.Predicate;
public class TextRow extends ValueRow {
	private final UiTextField field;
	private final Function<String, Object> parser;
	private final Predicate<String> validator;
	private final Function<Object, String> formatter;
	public TextRow(RowContext ctx, ValueConfigNode<?, ?> node, Function<String, Object> parser, Predicate<String> validator, Function<Object, String> formatter) {
		this(ctx, node, parser, validator, formatter, Integer.MAX_VALUE);
	}
	public TextRow(RowContext ctx, ValueConfigNode<?, ?> node, Function<String, Object> parser, Predicate<String> validator, Function<Object, String> formatter, int maxLength) {
		super(ctx, node);
		this.parser = parser;
		this.validator = validator;
		this.formatter = formatter;
		field = new UiTextField(formatter.apply(value()));
		field.setMaxLength(maxLength);
		field.setFilter(validator);
		field.setResponder(this::onInput);
		actionBar.add(field, UiTheme.CONTROL_WIDTH);
	}
	private void onInput(String text) {
		if (!validator.test(text)) return;
		try {
			setValue(parser.apply(text));
		} catch (RuntimeException ignored) {}
		ctx.refresh();
	}
	@Override
	public void refresh() {
		super.refresh();
		field.setError(state.validationError != null);
		var expected = formatter.apply(value());
		if (field.value().equals(expected)) return;
		try {
			if (parser.apply(field.value()).equals(value())) return;
		} catch (RuntimeException ignored) {}
		field.setValue(expected);
	}
	@Override
	public void resetToDefault() {
		super.resetToDefault();
		field.setValue(formatter.apply(value()));
	}
	@Override
	public void resetToActive() {
		super.resetToActive();
		field.setValue(formatter.apply(value()));
	}
}
