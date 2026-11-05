package io.github.forgestove.flexconfig.client.ui.row;
import io.github.forgestove.flexconfig.client.ui.core.*;
import io.github.forgestove.flexconfig.client.ui.model.RowContext;
import io.github.forgestove.flexconfig.tree.CategoryConfigNode;
/** 可折叠的分类标题行。 */
public class SectionRow extends ConfigRow {
	private final CategoryConfigNode<Object> category;
	private final Runnable onToggle;
	private boolean pressed;
	@SuppressWarnings("unchecked")
	public SectionRow(RowContext ctx, CategoryConfigNode<?> category, int depth, Runnable onToggle) {
		super(ctx);
		this.category = (CategoryConfigNode<Object>) category;
		this.depth = depth;
		this.onToggle = onToggle;
	}
	@Override
	public int height() {
		return UiTheme.SECTION_HEIGHT;
	}
	@Override
	public void render(UiContext ui) {
		var painter = ui.painter;
		if (pressed) painter.rect(bounds(), UiTheme.ROW_PRESS);
		var text = painter.trim(category.getTitle().getString().toUpperCase(), bounds().width() - UiTheme.PADDING * 2);
		var ty = bounds().bottom() - 13;
		var textX = bounds().x() + UiTheme.INDENT + depth * UiTheme.INDENT;
		painter.textEmphasis(text, textX, ty, UiTheme.TEXT);
		var ruleX = textX + painter.textWidthEmphasis(text) + 4;
		if (ruleX < bounds().right() - UiTheme.PADDING) painter.groove(ruleX, bounds().right() - UiTheme.PADDING, ty + 4);
		if (ui.tooltip() == null && hovered(ui) && category.getTooltip() != null) ui.tooltip(category.getTooltip(), bounds());
	}
	@Override
	public boolean mouseClicked(UiContext ui, double mouseX, double mouseY, int button) {
		if (button != 0 || !hovered(ui)) return false;
		pressed = true;
		return true;
	}
	@Override
	public boolean mouseReleased(UiContext ui, double mouseX, double mouseY, int button) {
		if (!pressed) return false;
		pressed = false;
		if (button != 0 || !hovered(ui)) return true;
		UiSound.click();
		onToggle.run();
		return true;
	}
}
