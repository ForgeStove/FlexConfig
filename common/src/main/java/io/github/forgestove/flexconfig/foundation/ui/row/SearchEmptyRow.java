package io.github.forgestove.flexconfig.foundation.ui.row;
import io.github.forgestove.flexconfig.foundation.client.Translation;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.model.RowContext;
import io.github.forgestove.flexconfig.foundation.ui.widget.*;
import net.minecraft.network.chat.Component;

import java.util.List;
/** 搜索无结果时的提示行：左侧说明，右侧清除按钮。 */
public class SearchEmptyRow extends ConfigRow {
	private static final int CLEAR_WIDTH = 48;
	private final Component message;
	private final UiButton clear;
	public SearchEmptyRow(RowContext ctx, Component message, Runnable onClear) {
		super(ctx);
		this.message = message;
		clear = new UiButton(Translation.SEARCH_CLEAR, UiButton.Style.SECONDARY, onClear);
	}
	@Override
	public void layout() {
		var b = bounds();
		clear.setBounds(new UiBounds(
			b.right() - UiTheme.PADDING - CLEAR_WIDTH,
			b.y() + (b.height() - UiTheme.CONTROL_HEIGHT) / 2,
			CLEAR_WIDTH,
			UiTheme.CONTROL_HEIGHT
		));
	}
	@Override
	public void render(UiContext ui) {
		var b = bounds();
		var painter = ui.painter;
		painter.groove(b.x(), b.right(), b.bottom() - 2);
		var textX = b.x() + UiTheme.INDENT;
		var text = painter.trim(message.getString(), clear.bounds().x() - 8 - textX);
		painter.text(text, textX, b.y() + (b.height() - painter.lineHeight()) / 2, UiTheme.TEXT);
		clear.render(ui);
	}
	@Override
	public List<UiControl> controls() {
		return List.of(clear);
	}
}
