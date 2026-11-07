package io.github.forgestove.flexconfig.foundation.ui.widget;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import java.util.function.IntSupplier;
public class UiColorSwatch extends UiControl {
	private final boolean alpha;
	private final IntSupplier color;
	private final Runnable onClick;
	public UiColorSwatch(boolean alpha, IntSupplier color, Runnable onClick) {
		this.alpha = alpha;
		this.color = color;
		this.onClick = onClick;
	}
	@Override
	public void render(UiContext ctx) {
		if (!visible) return;
		var argb = enabled ? color.getAsInt() : 0xFF555555;
		if (alpha) checkerboard(ctx, bounds());
		ctx.painter.rect(bounds(), argb);
		ctx.painter.border(bounds(), hovered(ctx) ? UiTheme.BORDER_LIGHT : UiTheme.BORDER);
		if (down) ctx.painter.border(bounds(), ctx.accent().light());
		showTooltip(ctx);
	}
	private static void checkerboard(UiContext ctx, UiBounds bounds) {
		var size = 4;
		var columns = (bounds.width() + size - 1) / size;
		var rows = (bounds.height() + size - 1) / size;
		for (var index = 0; index < columns * rows; index++) {
			var column = index % columns;
			var row = index / columns;
			var x = bounds.x() + column * size;
			var y = bounds.y() + row * size;
			var light = (column + row) % 2 == 0;
			ctx.painter.rect(x, y, Math.min(size, bounds.right() - x), Math.min(size, bounds.bottom() - y), light ? 0xFFBBBBBB : 0xFF888888);
		}
	}
	@Override
	public boolean mouseClicked(UiContext ctx, double mouseX, double mouseY, int button) {
		if (button != 0 || !focusable() || !hovered(ctx)) return false;
		ctx.focus.set(this);
		down = true;
		return true;
	}
	@Override
	public boolean mouseReleased(UiContext ctx, double mouseX, double mouseY, int button) {
		if (!down) return false;
		down = false;
		if (button != 0 || !enabled || !bounds().contains(mouseX, mouseY)) return true;
		UiSound.click();
		onClick.run();
		return true;
	}
}
