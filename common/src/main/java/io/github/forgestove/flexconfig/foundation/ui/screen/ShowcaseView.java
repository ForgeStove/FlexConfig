package io.github.forgestove.flexconfig.foundation.ui.screen;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
public class ShowcaseView {
	private final @Nullable ResourceLocation icon;
	public ShowcaseView(@Nullable ResourceLocation icon) {
		this.icon = icon;
	}
	public void render(UiContext ui, UiBounds bounds) {
		if (icon == null || bounds.width() <= 0 || bounds.height() <= 0) return;
		var painter = ui.painter;
		painter.gradient(bounds, UiTheme.TRACK_LIGHT, UiTheme.WELL);
		painter.border(bounds, UiTheme.WINDOW_EDGE);
		var area = bounds.inset(4, 4);
		var size = painter.imageSize(icon);
		if (size == null || size[0] <= 0 || size[1] <= 0) return;
		// 按原始宽高比等比缩放到可用区域内，避免非正方形图标被拉扁
		var scale = Math.min(area.width() / (float) size[0], area.height() / (float) size[1]);
		var drawWidth = Math.max(1, Math.round(size[0] * scale));
		var drawHeight = Math.max(1, Math.round(size[1] * scale));
		var x = area.x() + (area.width() - drawWidth) / 2;
		var y = area.y() + (area.height() - drawHeight) / 2;
		painter.rect(x, y, drawWidth, drawHeight, UiTheme.WELL);
		painter.image(icon, new UiBounds(x, y, drawWidth, drawHeight));
		painter.border(x, y, drawWidth, drawHeight, UiTheme.WINDOW_EDGE);
	}
}
