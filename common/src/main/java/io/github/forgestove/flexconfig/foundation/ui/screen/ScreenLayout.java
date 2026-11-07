package io.github.forgestove.flexconfig.foundation.ui.screen;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import net.minecraft.util.Mth;
/** 窗口布局：标题栏、左侧栏（操作按钮在上、分类在下）和右侧内容。 */
public class ScreenLayout {
	public final UiBounds panel, header, sidebar, showcase, tabs, footer, pageHeader, content, scrollbar;
	public ScreenLayout(int screenWidth, int screenHeight, boolean showShowcase) {
		var margin = screenWidth < 480 ? 6 : 12;
		var width = Mth.clamp(screenWidth - margin * 2, UiTheme.MIN_PANEL_WIDTH, UiTheme.MAX_PANEL_WIDTH);
		var height = Mth.clamp(screenHeight - margin * 2, UiTheme.MIN_PANEL_HEIGHT, UiTheme.MAX_PANEL_HEIGHT);
		width = Math.min(width, screenWidth - margin * 2);
		height = Math.min(height, screenHeight - margin * 2);
		var x = (screenWidth - width) / 2;
		var y = (screenHeight - height) / 2;
		panel = new UiBounds(x, y, width, height);
		header = new UiBounds(x, y, width, UiTheme.HEADER_HEIGHT);
		var bodyY = header.bottom();
		var leftWidth = Mth.clamp(Math.round(width * 0.3F), UiTheme.LEFT_MIN, UiTheme.LEFT_MAX);
		sidebar = new UiBounds(x, bodyY, leftWidth, panel.bottom() - bodyY);
		showcase = showShowcase ? new UiBounds(x + 4, bodyY + 4, leftWidth - 8, 56) : UiBounds.EMPTY;
		var footerY = (showShowcase ? showcase.bottom() : bodyY) + 4;
		footer = new UiBounds(x + 4, footerY, leftWidth - 8, 18);
		tabs = new UiBounds(x, footer.bottom() + 2, leftWidth, Math.max(0, panel.bottom() - footer.bottom() - 4));
		var contentX = sidebar.right() + 6;
		var contentRight = panel.right() - UiTheme.PADDING - UiTheme.SCROLLBAR_WIDTH - 2;
		pageHeader = new UiBounds(contentX, bodyY + 2, Math.max(0, contentRight - contentX), UiTheme.PAGE_HEADER);
		content = new UiBounds(
			contentX,
			pageHeader.bottom() + 1,
			Math.max(0, contentRight - contentX),
			Math.max(0, panel.bottom() - pageHeader.bottom() - 5)
		);
		scrollbar = new UiBounds(
			panel.right() - UiTheme.PADDING - UiTheme.SCROLLBAR_WIDTH,
			content.y(),
			UiTheme.SCROLLBAR_WIDTH,
			content.height()
		);
	}
}
