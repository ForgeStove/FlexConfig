package io.github.forgestove.flexconfig.client.gui.im.widget;
import io.github.forgestove.flexconfig.client.ClientUtil;
import io.github.forgestove.flexconfig.client.gui.im.core.*;
import net.minecraft.network.chat.Component;
/** 可选择的 chip，用于搜索过滤器等。 */
public class UiChip extends UiControl {
	private final UiAnim hover = new UiAnim(0);
	private Component label;
	private boolean selected;
	private final Runnable action;
	public UiChip(Component label, Runnable action) {
		this.label = label;
		this.action = action;
	}
	public void setLabel(Component label) {
		this.label = label;
	}
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	public int preferredWidth() {
		return ClientUtil.mc.font.width(label) + 14;
	}
	@Override
	public void render(UiContext ctx) {
		if (!visible) return;
		hover.target(focusable() && (hovered(ctx) || down) ? 1 : 0);
		hover.update(ctx.delta);
		var accent = ctx.accent();
		var pressed = selected || down;
		var face = !enabled ? 0xFF6E6A5C
			: selected ? down ? accent.darkest() : hover.get() > 0.5 ? accent.base() : accent.strong()
			: down ? UiTheme.SECONDARY_PRESSED : hover.get() > 0.5 ? UiTheme.SECONDARY_HOVER : UiTheme.SECONDARY_FACE;
		var highlight = selected ? accent.light() : UiTheme.SECONDARY_TOP;
		var shine = selected ? 0xFFF7F7F9 : UiTheme.SECONDARY_SHINE;
		var depth = selected ? accent.darkest() : UiTheme.SECONDARY_LIP;
		var inner = ctx.painter.bevel(bounds(), UiTheme.WINDOW_EDGE, face, highlight, shine, depth, 2, pressed);
		var text = ctx.painter.trim(label.getString(), bounds().width() - 10);
		var ink = selected ? accent.ink() : UiTheme.TITLE_INK;
		ctx.painter.text(text, bounds().x() + (bounds().width() - ctx.painter.textWidth(text)) / 2, inner.y() + (inner.height() - 7) / 2, ink);
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
