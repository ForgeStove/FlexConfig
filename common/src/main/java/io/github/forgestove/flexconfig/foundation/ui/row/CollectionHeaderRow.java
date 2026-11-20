package io.github.forgestove.flexconfig.foundation.ui.row;
import io.github.forgestove.flexconfig.foundation.tree.ValueConfigNode;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.model.*;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiButton;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiButton.Style;
public class CollectionHeaderRow extends ValueRow {
	private final UiButton add;
	public CollectionHeaderRow(RowContext ctx, ValueConfigNode node) {
		super(ctx, node);
		add = new UiButton(UiText.literal("+"), Style.PRIMARY, () -> CollectionRows.add(ctx, node));
		actionBar.add(add, UiTheme.ACTION_SIZE);
	}
	@Override
	public void refresh() {
		super.refresh();
		add.setEnabled(!state.readOnly && node.getElementType() != null);
	}
	@Override
	public void resetToDefault() {
		if (state.readOnly) return;
		ctx.exitElements(
			node.getPath(), () -> {
				super.resetToDefault();
				ctx.handler().clearElementKeybinds(node.getPath());
			}
		);
	}
	@Override
	public void resetToActive() {
		ctx.exitElements(
			node.getPath(), () -> {
				super.resetToActive();
				ctx.handler().clearElementKeybinds(node.getPath());
			}
		);
	}
}
