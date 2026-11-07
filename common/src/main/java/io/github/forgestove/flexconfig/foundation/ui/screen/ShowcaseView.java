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
		var side = Math.min(area.width(), area.height());
		var x = area.x() + (area.width() - side) / 2;
		var y = area.y() + (area.height() - side) / 2;
		painter.rect(x, y, side, side, UiTheme.WELL);
		painter.texture(icon, new UiBounds(x, y, side, side));
		painter.border(x, y, side, side, UiTheme.WINDOW_EDGE);
	}
}
