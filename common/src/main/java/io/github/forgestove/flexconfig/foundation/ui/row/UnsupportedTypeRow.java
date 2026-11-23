package io.github.forgestove.flexconfig.foundation.ui.row;
import io.github.forgestove.flexconfig.foundation.client.Translation;
import io.github.forgestove.flexconfig.foundation.tree.ValueConfigNode;
import io.github.forgestove.flexconfig.foundation.ui.core.UiStyle;
import io.github.forgestove.flexconfig.foundation.ui.model.RowContext;
public class UnsupportedTypeRow extends InfoRow {
	public UnsupportedTypeRow(RowContext ctx, ValueConfigNode node, int depth) {
		super(ctx, Translation.UNSUPPORTED_TYPE.copy().append(node.getValueType().getSimpleName()).withStyle(UiStyle.RED), depth, true);
	}
}
