package io.github.forgestove.flexconfig.foundation.ui.screen;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
/** 左栏顶部的预览：注册了图标才显示。 */
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
		renderIcon(ui, bounds);
	}
	private void renderIcon(UiContext ui, UiBounds bounds) {
		var painter = ui.painter;
		var area = bounds.inset(4, 4);
		var size = painter.textureSize(icon);
		if (size == null) return;
		// 按原始宽高比等比缩放到可用区域内，避免非正方形图标被拉扁
		var scale = Math.min(area.width() / (float) size[0], area.height() / (float) size[1]);
		var width = Math.max(1, Math.round(size[0] * scale));
		var height = Math.max(1, Math.round(size[1] * scale));
		var x = area.x() + (area.width() - width) / 2;
		var y = area.y() + (area.height() - height) / 2;
		painter.rect(x, y, width, height, UiTheme.WELL);
		painter.texture(icon, new UiBounds(x, y, width, height));
		painter.border(x, y, width, height, UiTheme.WINDOW_EDGE);
	}
}
