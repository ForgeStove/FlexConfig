package io.github.forgestove.flexconfig.client.ui.row;
import io.github.forgestove.flexconfig.client.ui.core.UiTheme;
import io.github.forgestove.flexconfig.client.ui.model.RowContext;
import io.github.forgestove.flexconfig.client.ui.widget.UiTextField;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;

import java.util.function.*;
/** 数值文本行，按类型提供 parser/validator/formatter；拖动或重置后由 refresh 回填文本。 */
public class NumberRow extends ValueRow {
	private final UiTextField field;
	private final Function<String, Object> parser;
	private final Predicate<String> validator;
	private final Function<Object, String> formatter;
	private boolean parseError;
	public NumberRow(RowContext ctx, ValueConfigNode<?, ?> node, Function<String, Object> parser, Predicate<String> validator, Function<Object, String> formatter) {
		super(ctx, node);
		this.parser = parser;
		this.validator = validator;
		this.formatter = formatter;
		field = new UiTextField(formatter.apply(value()));
		field.setFilter(validator);
		field.setResponder(this::onInput);
		actionBar.add(field, UiTheme.CONTROL_WIDTH);
	}
	@Override
	protected String defaultText() {
		var value = node.getDefaultValue();
		return value == null ? "" : formatter.apply(value);
	}
	private void onInput(String text) {
		if (!validator.test(text)) {
			parseError = true;
			ctx.refresh();
			return;
		}
		try {
			setValue(parser.apply(text));
			parseError = false;
		} catch (RuntimeException e) {
			parseError = true;
		}
		ctx.refresh();
	}
	@Override
	public void refresh() {
		super.refresh();
		field.setError(parseError || state.validationError != null);
		if (parseError) return;   // 正在输入的半成品（"1." / "-"）不能被格式化回填
		var expected = formatter.apply(value());
		if (field.value().equals(expected)) return;
		try {
			if (parser.apply(field.value()).equals(value())) return;   // 用户输入等价文本（如 01），不打断
		} catch (RuntimeException ignored) {}
		field.setValue(expected);
	}
	@Override
	public boolean hasError() {
		return parseError || super.hasError();
	}
	@Override
	public void resetToDefault() {
		parseError = false;
		super.resetToDefault();
	}
	@Override
	public void resetToActive() {
		parseError = false;
		super.resetToActive();
	}
}
