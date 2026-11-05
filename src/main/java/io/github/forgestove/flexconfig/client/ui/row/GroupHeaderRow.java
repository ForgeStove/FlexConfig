package io.github.forgestove.flexconfig.client.ui.row;
import io.github.forgestove.flexconfig.client.ui.core.*;
import io.github.forgestove.flexconfig.client.ui.model.RowContext;
import io.github.forgestove.flexconfig.client.ui.widget.*;
import io.github.forgestove.flexconfig.client.ui.widget.UiButton.Style;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.*;
public class GroupHeaderRow extends ConfigRow {
	private final Component label;
	private final @Nullable UiButton delete;
	public GroupHeaderRow(RowContext ctx, Component label, int depth, boolean canDelete, @Nullable Runnable onDelete) {
		super(ctx);
		this.label = label;
		this.depth = depth;
		delete = onDelete == null ? null : new UiButton(Component.literal("x"), Style.DANGER, onDelete);
		if (delete != null) {
			delete.setEnabled(canDelete);
			delete.setVisible(true);
		}
	}
	@Override
	public void layout() {
		if (delete != null) {
			var height = Math.min(bounds().height(), UiTheme.CONTROL_HEIGHT);
			delete.setBounds(new UiBounds(bounds().right() - UiTheme.PADDING - UiTheme.ACTION_SIZE, bounds().y() + (bounds().height() - height) / 2, UiTheme.ACTION_SIZE, height));
		}
	}
	@Override
	public int height() {
		return UiTheme.GROUP_HEIGHT;
	}
	@Override
	public void render(UiContext ui) {
		var painter = ui.painter;
		var text = painter.trim(label.getString().toUpperCase(), bounds().width() - UiTheme.PADDING * 2 - 18);
		var ty = bounds().bottom() - 13;
		painter.text(text, bounds().x() + UiTheme.INDENT + depth * UiTheme.INDENT, ty, UiTheme.TEXT_2);
		var ruleX = bounds().x() + UiTheme.INDENT + depth * UiTheme.INDENT + painter.textWidth(text) + 4;
		if (ruleX < bounds().right() - UiTheme.PADDING - 18) painter.groove(ruleX, bounds().right() - UiTheme.PADDING - 18, ty + 4);
		if (delete != null) delete.render(ui);
	}
	@Override
	public List<UiControl> controls() {
		return delete == null ? List.of() : List.of(delete);
	}
}
