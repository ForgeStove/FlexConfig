package io.github.forgestove.flexconfig.foundation.ui.widget;
import io.github.forgestove.flexconfig.api.menu.Accent;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
public class UiButton extends UiControl {
	public enum Style {
		PRIMARY,
		SECONDARY,
		DANGER,
		GHOST,
		TITLE,
		KEYBIND
	}
	private final UiAnim hover = new UiAnim(0);
	private Component label;
	private boolean active;
	private static final Palette PRIMARY_DISABLED = new Palette(0xFF6E6A5C, 0xFF6E6A5C, 0xFF6E6A5C, 0xFF58585A, 0xFF6E6A5C, 0xFF6E6A5C);
	private final Style style;
	private final Runnable action;
	public UiButton(@NotNull Component label, @NotNull Style style, @NotNull Runnable action) {
		this.label = label;
		this.style = style;
		this.action = action;
	}
	public void setLabel(Component label) {
		this.label = label;
	}
	/** 仅按键绑定样式使用：捕获中时把凹槽描边换成主题色。 */
	public void setActive(boolean active) {
		this.active = active;
	}
	@Override
	public void render(UiContext ctx) {
		if (!visible) return;
		hover.target(focusable() && (hovered(ctx) || down) ? 1 : 0);
		hover.update(ctx.delta);
		var painter = ctx.painter;
		if (style == Style.KEYBIND) {
			drawKeybind(ctx);
			showTooltip(ctx);
			return;
		}
		if (style == Style.GHOST || style == Style.TITLE) {
			if (hover.get() > 0.01 || down) painter.rect(bounds(), style == Style.TITLE ? 0x33000000 : UiTheme.ROW_HOVER);
			var color = !enabled ? UiTheme.DISABLED : style == Style.TITLE ? UiTheme.TITLE_INK : UiTheme.TEXT_2;
			var text = displayLabel(painter);
			var width = painter.textWidth(text);
			var x = bounds().x() + Math.round((bounds().width() - width) / 2F);
			painter.text(text, x, bounds().y() + Math.round((bounds().height() - painter.lineHeight()) / 2F) + 1, color);
			showTooltip(ctx);
			return;
		}
		UiBounds face;
		if (style == Style.PRIMARY) {
			var palette = enabled ? palette(ctx.accent()) : PRIMARY_DISABLED;
			var faceColor = !enabled ? palette.face : down ? palette.pressed : hover.get() > 0.5 ? palette.hover : palette.face;
			face = painter.bevel(bounds(), UiTheme.WINDOW_EDGE, faceColor, palette.highlight, palette.shine, palette.depth, 2, down);
			drawLabel(ctx, enabled ? ctx.accent().ink() : 0xFF3A3830, face);
		} else {
			var palette = palette(ctx.accent());
			face = painter.bevel(
				bounds(),
				UiTheme.WINDOW_EDGE,
				fade(down ? palette.pressed : hover.get() > 0.5 ? palette.hover : palette.face),
				fade(palette.highlight),
				fade(palette.shine),
				fade(palette.depth),
				2,
				down
			);
			drawLabel(ctx, fade(style == Style.DANGER ? 0xFFFFFFFF : UiTheme.TITLE_INK), face);
		}
		showTooltip(ctx);
	}
	private Component displayLabel(UiPainter painter) {
		var component = style == Style.PRIMARY
			? Component.literal(label.getString().toUpperCase(Locale.ROOT)).withStyle(label.getStyle())
			: label;
		var max = bounds().width() - 8;
		if (painter.textWidth(component) <= max) return component;
		return Component.literal(painter.trim(component.getString(), max)).withStyle(component.getStyle());
	}
	/** 按键绑定：输入框式凹槽里居中显示键名，捕获中把描边换成主题色。 */
	private void drawKeybind(UiContext ctx) {
		var painter = ctx.painter;
		var b = bounds();
		painter.well(b, active ? ctx.accent().base() : UiTheme.WINDOW_EDGE, 0.3F * (float) hover.get());
		var text = displayLabel(painter).getString();
		var width = painter.textWidth(text);
		var x = b.x() + Math.round((b.width() - width) / 2F);
		var y = b.y() + Math.round((b.height() - 8) / 2F);
		painter.text(text, x, y, enabled ? UiTheme.TEXT : UiTheme.DISABLED);
	}
	private void drawLabel(UiContext ctx, int color, UiBounds face) {
		var painter = ctx.painter;
		var text = displayLabel(painter);
		var width = painter.textWidth(text);
		var x = bounds().x() + Math.round((bounds().width() - width) / 2F);
		var y = face.y() + Math.round((face.height() - 8) / 2F);
		painter.text(text, x, y, color);
	}
	private int fade(int color) {
		return enabled ? color : UiTheme.fadeToward(color, UiTheme.PANEL, 0.4F);
	}
	private Palette palette(Accent accent) {
		return switch (style) {
			case PRIMARY ->
				new Palette(accent.base(), accent.light(), accent.lightest(), accent.darkest(), accent.bright(), accent.strong());
			case DANGER -> new Palette(
				UiTheme.DANGER,
				UiTheme.DANGER_LIGHT,
				UiTheme.DANGER_LIGHT,
				UiTheme.DANGER_DEPTH,
				UiTheme.DANGER_LIGHT,
				0xFF9E3A2F
			);
			default -> new Palette(
				UiTheme.SECONDARY_FACE,
				UiTheme.SECONDARY_TOP,
				UiTheme.SECONDARY_SHINE,
				UiTheme.SECONDARY_LIP,
				UiTheme.SECONDARY_HOVER,
				UiTheme.SECONDARY_PRESSED
			);
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
