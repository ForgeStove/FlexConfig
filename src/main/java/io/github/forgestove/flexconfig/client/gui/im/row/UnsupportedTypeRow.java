package io.github.forgestove.flexconfig.client.gui.im.row;
import io.github.forgestove.flexconfig.client.Translation;
import io.github.forgestove.flexconfig.client.gui.im.model.RowContext;
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
