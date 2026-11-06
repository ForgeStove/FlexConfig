package io.github.forgestove.flexconfig.client.ui.widget;
import io.github.forgestove.flexconfig.client.ui.core.*;
import net.minecraft.network.chat.Component;
/** 小图标按钮（关闭等）。 */
public class UiGlyphButton extends UiControl {
	public enum Glyph {
		CLOSE
	}
	private final UiAnim hover = new UiAnim(0);
	private final Glyph glyph;
	private final Runnable action;
	public UiGlyphButton(Glyph glyph, Runnable action) {
		this.glyph = glyph;
		this.action = action;
		tooltip(glyph == Glyph.CLOSE ? Component.translatable("flexconfig.close") : Component.empty());
	}
	@Override
	public void render(UiContext ctx) {
		if (!visible) return;
		hover.target(focusable() && (hovered(ctx) || down) ? 1 : 0);
		hover.update(ctx.delta);
		if (hover.get() > 0.01 || down) {
			var alpha = (int) Math.round((down ? 1F : hover.get() * 0.55F) * 255);
			ctx.painter.rect(bounds(), alpha << 24 | 0xB1B2B5);
		}
		var painter = ctx.painter;
		var x = bounds().x() + Math.round((bounds().width() - 12) / 2F);
		var y = bounds().y() + Math.round((bounds().height() - 12) / 2F);
		if (glyph == Glyph.CLOSE) for (var i = 0; i < 12; i += 2) {
			painter.rect(x + i, y + i, 2, 2, UiTheme.WINDOW_EDGE);
			painter.rect(x + 10 - i, y + i, 2, 2, UiTheme.WINDOW_EDGE);
		}
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
		action.run();
		return true;
	}
	@Override
	public boolean keyPressed(UiContext ctx, int keyCode, int scanCode, int modifiers) {
		if (!focusable() || !ctx.focus.is(this) || keyCode != 257 && keyCode != 32) return false;
		UiSound.click();
		action.run();
		return true;
	}
}
