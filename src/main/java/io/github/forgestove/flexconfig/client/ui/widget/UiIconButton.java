package io.github.forgestove.flexconfig.client.ui.widget;
import io.github.forgestove.flexconfig.client.ui.FlexGuiTextures;
import io.github.forgestove.flexconfig.client.ui.core.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
public class UiIconButton extends UiControl {
	private final UiAnim hover = new UiAnim(0);
	private FlexGuiTextures icon;
	private final Runnable action;
	public UiIconButton(FlexGuiTextures icon, Runnable action) {
		this.icon = icon;
		this.action = action;
	}
	public void setIcon(FlexGuiTextures icon) {
		this.icon = icon;
	}
	public void setTooltip(@Nullable Component tooltip) {
		tooltip(tooltip);
	}
	@Override
	public void render(UiContext ctx) {
		if (!visible) return;
		hover.target(focusable() && (hovered(ctx) || down) ? 1 : 0);
		hover.update(ctx.delta);
		var painter = ctx.painter;
		painter.bevel(
			bounds(),
			UiTheme.WINDOW_EDGE,
			fade(down ? UiTheme.SECONDARY_PRESSED : hover.get() > 0.5 ? UiTheme.SECONDARY_HOVER : UiTheme.SECONDARY_FACE),
			fade(UiTheme.SECONDARY_TOP),
			fade(UiTheme.SECONDARY_SHINE),
			fade(UiTheme.SECONDARY_LIP),
			2,
			down
		);
		painter.icon(icon, bounds().offset(0, down ? 1 : -1), enabled ? UiTheme.ICON_INK : UiTheme.DISABLED);
		showTooltip(ctx);
	}
	private int fade(int color) {
		return enabled ? color : UiTheme.fadeToward(color, UiTheme.PANEL, 0.4F);
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
