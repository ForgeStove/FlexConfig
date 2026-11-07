package io.github.forgestove.flexconfig.foundation.ui.row;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.model.RowContext;
import net.minecraft.network.chat.Component;
import java.util.Locale;
/** 搜索结果里的分类标题。 */
public class SearchHeaderRow extends ConfigRow {
	private final Component title;
	public SearchHeaderRow(RowContext ctx, Component title, int depth) {
		super(ctx);
		this.title = title;
		this.depth = depth;
	}
	@Override
	public int height() {
		return UiTheme.SECTION_HEIGHT;
	}
	@Override
	public void render(UiContext ui) {
		var text = title.getString().toUpperCase(Locale.ROOT);
		var ty = bounds().bottom() - 13;
		var textX = bounds().x() + UiTheme.INDENT + depth * UiTheme.INDENT;
		ui.painter.textEmphasis(text, textX, ty, UiTheme.TEXT);
		var ruleX = textX + ui.painter.textWidthEmphasis(text) + 4;
		if (ruleX < bounds().right() - UiTheme.PADDING) ui.painter.groove(ruleX, bounds().right() - UiTheme.PADDING, ty + 4);
	}
}
