package io.github.forgestove.flexconfig.client.gui.im.widget;
import io.github.forgestove.flexconfig.client.gui.im.core.*;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
/** 分段选择：少量选项时替代下拉。 */
public class UiSegmented extends UiControl {
	private final List<Component> labels;
	private final IntSupplier selected;
	private final IntConsumer onSelect;
	private final UiAnim hover = new UiAnim(0, UiMotion.CONTROL);
	private int pressedIndex = -1;
	public UiSegmented(List<Component> labels, IntSupplier selected, IntConsumer onSelect) {
		this.labels = labels;
		this.selected = selected;
		this.onSelect = onSelect;
	}
	@Override
	public void render(UiContext ctx) {
		if (!visible || labels.isEmpty()) return;
		hover.target(focusable() && (hovered(ctx) || down) ? 1 : 0);
		hover.update(ctx.delta);
		var n = labels.size();
		var segW = Math.max(1, bounds().width() / n);
		for (var i = 0; i < n; i++) {
			var x = bounds().x() + i * segW;
			var right = i == n - 1 ? bounds().right() : x + segW - 1;
			var seg = new UiBounds(x, bounds().y(), Math.max(1, right - x), bounds().height());
			var isSelected = i == selected.getAsInt();
			var accent = ctx.accent();
			var isPressed = pressedIndex == i && down;
			UiBounds inner;
			if (isSelected) inner = ctx.painter.bevel(seg, UiTheme.WINDOW_EDGE, accent.strong(), accent.light(), 0xFFF7F7F9, accent.darkest(), 2, true);
			else {
				var face = isPressed ? UiTheme.SECONDARY_PRESSED : seg.contains(ctx.mouseX, ctx.mouseY) ? UiTheme.SECONDARY_HOVER : UiTheme.SECONDARY_FACE;
				inner = ctx.painter.bevel(seg, UiTheme.WINDOW_EDGE, face, UiTheme.SECONDARY_TOP, UiTheme.SECONDARY_SHINE, UiTheme.SECONDARY_LIP, 2, isPressed);
			}
			var text = ctx.painter.trim(labels.get(i).getString(), seg.width() - 4);
			ctx.painter.text(text, seg.x() + (seg.width() - ctx.painter.textWidth(text)) / 2, inner.y() + (inner.height() - 7) / 2, isSelected ? accent.ink() : UiTheme.TITLE_INK);
		}
	}
	@Override
	public boolean mouseClicked(UiContext ctx, double mouseX, double mouseY, int button) {
		if (button != 0 || !focusable() || !hovered(ctx)) return false;
		ctx.focus.set(this);
		pressedIndex = indexAt(mouseX);
		down = true;
		return true;
	}
	@Override
	public boolean mouseReleased(UiContext ctx, double mouseX, double mouseY, int button) {
		if (!down) return false;
		down = false;
		var index = pressedIndex;
		pressedIndex = -1;
		if (button != 0 || !enabled || !bounds().contains(mouseX, mouseY)) return true;
		if (index >= 0 && index != selected.getAsInt()) {
			UiSound.click();
			onSelect.accept(index);
		}
		return true;
	}
	@Override
	public boolean keyPressed(UiContext ctx, int keyCode, int scanCode, int modifiers) {
		if (!focusable() || !ctx.focus.is(this) || labels.isEmpty()) return false;
		var now = selected.getAsInt();
		Integer next = null;
		if (keyCode == 263) next = Math.max(0, now - 1);
		if (keyCode == 262) next = Math.min(labels.size() - 1, now + 1);
		if (keyCode == 257 || keyCode == 32) next = now;
		if (next == null) return false;
		if (next != now) onSelect.accept(next);
		UiSound.click();
		return true;
	}
	private int indexAt(double mouseX) {
		var segW = Math.max(1, bounds().width() / labels.size());
		return (int) UiMath.clamp((mouseX - bounds().x()) / segW, 0, labels.size() - 1);
	}
}
