package io.github.forgestove.flexconfig.client.ui.screen;
import io.github.forgestove.flexconfig.client.ui.core.*;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
/** 左栏顶部的预览：注册了图标就显示图标，否则显示示意图形。 */
public class ShowcaseView {
	private final @Nullable ResourceLocation icon;
	public ShowcaseView(@Nullable ResourceLocation icon) {
		this.icon = icon;
	}
	public void render(UiContext ui, UiBounds bounds) {
		if (bounds.width() <= 0 || bounds.height() <= 0) return;
		var painter = ui.painter;
		painter.gradient(bounds, UiTheme.TRACK_LIGHT, UiTheme.WELL);
		painter.border(bounds, UiTheme.WINDOW_EDGE);
		if (icon != null) {
			renderIcon(ui, bounds);
			return;
		}
		var t = Util.getMillis() / 1000D;
		var parallaxX = (int) Math.round((ui.mouseX / bounds.width() - 1) * 2);
		var parallaxY = (int) Math.round((ui.mouseY / bounds.height() - 1) * 2);
		var inner = bounds.inset(8, 8);
		var rows = 3;
		for (var i = 0; i < rows; i++) {
			var y = inner.y() + i * (inner.height() / rows) + 2;
			var track = new UiBounds(inner.x() + parallaxX, y, inner.width() - 4, 4);
			painter.rect(track, UiTheme.WINDOW_EDGE);
			painter.rect(track.x() + 1, track.y() + 1, Math.max(0, track.width() * (i + 2) / 5), 2, ui.accent().base());
			var knobX = track.x() + Math.round(track.width() * (float) (i + 2) / 5) + Math.round((float) Math.sin(t + i) * 2);
			painter.bevel(new UiBounds(knobX - 3, track.y() - 3 + parallaxY / 2, 6, 10), UiTheme.WINDOW_EDGE, UiTheme.KNOB, UiTheme.KNOB_HI, UiTheme.KNOB_HI, UiTheme.KNOB_LO, 1, false);
		}
		painter.rect(inner.x() + 2, inner.bottom() - 5, inner.width() / 3, 2, UiTheme.SECONDARY_FACE);
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
