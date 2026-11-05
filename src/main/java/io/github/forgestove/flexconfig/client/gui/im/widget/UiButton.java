package io.github.forgestove.flexconfig.client.gui.im.widget;
import io.github.forgestove.flexconfig.api.Accent;
import io.github.forgestove.flexconfig.client.gui.im.core.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import java.util.Locale;
public class UiButton extends UiControl {
	public enum Style {
		PRIMARY,
		SECONDARY,
		DANGER,
		GHOST,
		TITLE
	}
	private final UiAnim hover = new UiAnim(0);
	private Component label;
	private Style style;
	private final Runnable action;
	public UiButton(@NotNull Component label, @NotNull Style style, @NotNull Runnable action) {
		this.label = label;
		this.style = style;
		this.action = action;
	}
	public void setLabel(Component label) {
		this.label = label;
	}
	public void setStyle(Style style) {
		this.style = style;
	}
	@Override
	public void render(UiContext ctx) {
		if (!visible) return;
		hover.target(focusable() && (hovered(ctx) || down) ? 1 : 0);
		hover.update(ctx.delta);
		var painter = ctx.painter;
		if (style == Style.GHOST || style == Style.TITLE) {
			if (hover.get() > 0.01 || down) painter.rect(bounds(), style == Style.TITLE ? 0x33000000 : UiTheme.ROW_HOVER);
			var color = !enabled ? UiTheme.DISABLED : style == Style.TITLE ? UiTheme.TITLE_INK : UiTheme.TEXT_2;
			var text = trimmedLabel(painter);
			var x = bounds().x() + (bounds().width() - painter.textWidth(text)) / 2;
			painter.text(text, x, bounds().y() + (bounds().height() - painter.lineHeight()) / 2 + 1, color);
			showTooltip(ctx);
			return;
		}
		var palette = palette(ctx.accent());
		var faceColor = !enabled ? 0xFF6E6A5C : down ? palette.pressed : hover.get() > 0.5 ? palette.hover : palette.face;
		var face = painter.bevel(bounds(), UiTheme.WINDOW_EDGE, faceColor, palette.highlight, palette.shine, palette.depth, 2, down);
		drawLabel(ctx, enabled ? style == Style.SECONDARY ? UiTheme.TITLE_INK : ctx.accent().ink() : 0xFF3A3830, face);
		showTooltip(ctx);
	}
	private String trimmedLabel(UiPainter painter) {
		var label = style == Style.PRIMARY ? this.label.getString().toUpperCase(Locale.ROOT) : this.label.getString();
		return painter.trim(label, bounds().width() - 8);
	}
	private void drawLabel(UiContext ctx, int color, UiBounds face) {
		var painter = ctx.painter;
		var text = trimmedLabel(painter);
		var x = bounds().x() + (bounds().width() - painter.textWidth(text)) / 2;
		var capH = style == Style.PRIMARY ? 10 : 7;
		var y = face.y() + (face.height() - capH) / 2;
		painter.text(text, x, y, color);
	}
	private Palette palette(Accent accent) {
		return switch (style) {
			case PRIMARY -> new Palette(accent.base(), accent.light(), accent.lightest(), accent.darkest(), accent.bright(), accent.strong());
			case DANGER -> new Palette(UiTheme.DANGER, UiTheme.DANGER_LIGHT, UiTheme.DANGER_LIGHT, UiTheme.DANGER_DEPTH, UiTheme.DANGER_LIGHT, 0xFF9E3A2F);
			default -> new Palette(UiTheme.SECONDARY_FACE, UiTheme.SECONDARY_TOP, UiTheme.SECONDARY_SHINE, UiTheme.SECONDARY_LIP, UiTheme.SECONDARY_HOVER, UiTheme.SECONDARY_PRESSED);
		};
	}
	private record Palette(int face, int highlight, int shine, int depth, int hover, int pressed) {}
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
		if (!focusable() || !ctx.focus.is(this)) return false;
		if (keyCode != 257 && keyCode != 32) return false;   // Enter / Space
		UiSound.click();
		action.run();
		return true;
	}
}
