package io.github.forgestove.flexconfig.client.gui.im.row;
import io.github.forgestove.flexconfig.client.gui.im.core.UiTheme;
import io.github.forgestove.flexconfig.client.gui.im.model.*;
import io.github.forgestove.flexconfig.client.gui.im.widget.UiButton;
import io.github.forgestove.flexconfig.client.gui.im.widget.UiButton.Style;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import net.minecraft.network.chat.Component;
public class CollectionHeaderRow extends ValueRow {
	private final UiButton add;
	public CollectionHeaderRow(RowContext ctx, ValueConfigNode<?, ?> node) {
		super(ctx, node);
		add = new UiButton(Component.literal("+"), Style.SECONDARY, () -> CollectionRows.add(ctx, node));
		add.tooltip(Component.literal("+"));
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
