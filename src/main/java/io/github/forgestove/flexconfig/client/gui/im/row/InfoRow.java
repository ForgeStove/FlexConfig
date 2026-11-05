package io.github.forgestove.flexconfig.client.gui.im.row;
import io.github.forgestove.flexconfig.client.gui.im.core.*;
import io.github.forgestove.flexconfig.client.gui.im.model.RowContext;
import net.minecraft.network.chat.Component;
public class InfoRow extends ConfigRow {
	private final Component text;
	private final boolean error;
	public InfoRow(RowContext ctx, Component text, int depth, boolean error) {
		super(ctx);
		this.text = text;
		this.depth = depth;
		this.error = error;
	}
	@Override
	public void render(UiContext ui) {
		ui.painter.text(text, bounds().x() + UiTheme.INDENT + depth * UiTheme.INDENT, bounds().y() + (bounds().height() - ui.painter.lineHeight()) / 2, error ? UiTheme.DANGER : UiTheme.MUTED);
	}
}
