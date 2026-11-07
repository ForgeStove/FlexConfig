package io.github.forgestove.flexconfig.foundation.ui.widget;
import io.github.forgestove.flexconfig.foundation.ui.core.*;

import java.util.function.IntSupplier;
public class UiColorSwatch extends UiControl {
	private final IntSupplier color;
	private final Runnable onClick;
	public UiColorSwatch(IntSupplier color, Runnable onClick) {
		this.color = color;
		this.onClick = onClick;
	}
	@Override
	public void render(UiContext ctx) {
		if (!visible) return;
		var argb = (enabled ? color.getAsInt() : 0xFF555555) | 0xFF000000;   // 固定不透明，避免和行底色混合
		ctx.painter.rect(bounds(), argb);
		ctx.painter.border(bounds(), hovered(ctx) ? UiTheme.BORDER_LIGHT : UiTheme.BORDER);
		if (down) ctx.painter.border(bounds(), ctx.accent().light());
		showTooltip(ctx);
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
