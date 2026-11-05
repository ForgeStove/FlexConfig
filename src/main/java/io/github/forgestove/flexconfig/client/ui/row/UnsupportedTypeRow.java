package io.github.forgestove.flexconfig.client.ui.row;
import io.github.forgestove.flexconfig.client.Translation;
import io.github.forgestove.flexconfig.client.ui.model.RowContext;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
public class UnsupportedTypeRow extends InfoRow {
	public UnsupportedTypeRow(RowContext ctx, ValueConfigNode<?, ?> node, int depth) {
		super(
			ctx,
			Translation.UNSUPPORTED_TYPE.copy().append(node.getValueType().getSimpleName()).withStyle(ChatFormatting.RED),
			depth,
			true
		);
	}
}
