package io.github.forgestove.flexconfig.foundation.ui.screen;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import org.jetbrains.annotations.Nullable;
/** 左栏顶部的预览：注册了图标才显示。 */
public class ShowcaseView {
	private final @Nullable UiTexture icon;
	public ShowcaseView(@Nullable UiTexture icon) {
		this.icon = icon;
	}
	public void render(UiContext ui, UiBounds bounds) {
		if (icon == null || bounds.width() <= 0 || bounds.height() <= 0) return;
		var painter = ui.painter;
		painter.gradient(bounds, UiTheme.TRACK_LIGHT, UiTheme.WELL);
		painter.border(bounds, UiTheme.WINDOW_EDGE);
		renderIcon(ui, bounds);
	}
	private void renderIcon(UiContext ui, UiBounds bounds) {
		var painter = ui.painter;
		var area = bounds.inset(4, 4);
		var width = icon.width();
		var height = icon.height();
		if (width <= 0 || height <= 0) return;
		// 按原始宽高比等比缩放到可用区域内，避免非正方形图标被拉扁
		var scale = Math.min(area.width() / (float) width, area.height() / (float) height);
		var drawWidth = Math.max(1, Math.round(width * scale));
		var drawHeight = Math.max(1, Math.round(height * scale));
		var x = area.x() + (area.width() - drawWidth) / 2;
		var y = area.y() + (area.height() - drawHeight) / 2;
		painter.rect(x, y, drawWidth, drawHeight, UiTheme.WELL);
		painter.texture(icon, new UiBounds(x, y, drawWidth, drawHeight));
		painter.border(x, y, drawWidth, drawHeight, UiTheme.WINDOW_EDGE);
	}
}
