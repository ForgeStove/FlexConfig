package io.github.forgestove.flexconfig.foundation.ui.row;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.model.RowContext;
import io.github.forgestove.flexconfig.foundation.tree.CategoryConfigNode;
public class SectionRow extends ConfigRow {
	private final CategoryConfigNode<?> category;
	public SectionRow(RowContext ctx, CategoryConfigNode<?> category, int depth) {
		super(ctx);
		this.category = category;
		this.depth = depth;
	}
	@Override
	public int height() {
		return UiTheme.SECTION_HEIGHT;
	}
	@Override
	public void render(UiContext ui) {
		var painter = ui.painter;
		var text = painter.trim(category.getTitle().getString().toUpperCase(), bounds().width() - UiTheme.PADDING * 2);
		var ty = bounds().bottom() - 13;
		var textX = bounds().x() + UiTheme.INDENT + depth * UiTheme.INDENT;
		painter.textEmphasis(text, textX, ty, UiTheme.TEXT);
		var ruleX = textX + painter.textWidthEmphasis(text) + 4;
		if (ruleX < bounds().right() - UiTheme.PADDING) painter.groove(ruleX, bounds().right() - UiTheme.PADDING, ty + 4);
		if (hovered(ui)) ui.hover(this, category::getTooltip, bounds());
	}
}
