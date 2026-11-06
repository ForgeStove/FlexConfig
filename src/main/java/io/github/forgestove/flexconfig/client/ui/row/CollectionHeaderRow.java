package io.github.forgestove.flexconfig.client.ui.row;
import io.github.forgestove.flexconfig.client.ui.core.UiTheme;
import io.github.forgestove.flexconfig.client.ui.model.*;
import io.github.forgestove.flexconfig.client.ui.widget.UiButton;
import io.github.forgestove.flexconfig.client.ui.widget.UiButton.Style;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import net.minecraft.network.chat.Component;
public class CollectionHeaderRow extends ValueRow {
	private final UiButton add;
	public CollectionHeaderRow(RowContext ctx, ValueConfigNode<?, ?> node) {
		super(ctx, node);
		add = new UiButton(Component.literal("+"), Style.PRIMARY, () -> CollectionRows.add(ctx, node));
		actionBar.add(add, UiTheme.ACTION_SIZE);
	}
	@Override
	public void refresh() {
		super.refresh();
		add.setEnabled(!state.readOnly && node.getElementType() != null);
	}
	@Override
	public void resetToDefault() {
		ctx.handler().clearElementKeybinds(node.getPath());
		super.resetToDefault();
		ctx.rebuildTab();
	}
	@Override
	public void resetToActive() {
		ctx.handler().clearElementKeybinds(node.getPath());
		super.resetToActive();
		ctx.rebuildTab();
	}
}
