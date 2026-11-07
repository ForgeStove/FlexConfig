package io.github.forgestove.flexconfig.foundation.ui.widget;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
/** 分段选择：少量选项时替代下拉。 */
public class UiSegmented extends UiControl {
	private final List<Component> labels;
	private final IntConsumer onSelect;
	private final UiAnim center = new UiAnim(0, UiMotion.SELECT);
	private final List<UiTween> selectedness = new ArrayList<>();
	private int selected;
	private int pressedIndex = -1;
	public UiSegmented(List<Component> labels, IntSupplier selected, IntConsumer onSelect) {
		this.labels = labels;
		this.onSelect = onSelect;
		int value = selected.getAsInt();
		this.selected = Mth.clamp(value, 0, Math.max(0, labels.size() - 1));
		for (var i = 0; i < labels.size(); i++) selectedness.add(new UiTween(i == this.selected ? 1 : 0));
	}
	public void show(int index, boolean animate) {
		if (index != selected && index >= 0 && index < labels.size()) moveTo(index, animate);
	}
	private void moveTo(int index, boolean animate) {
		selectedness.get(selected).start(selectedness.get(selected).value(), 0, UiMotion.TOOLTIP_FADE);
		selected = index;
		selectedness.get(selected).start(selectedness.get(selected).value(), 1, UiMotion.TOOLTIP_FADE);
		if (animate) center.target(segmentCenter(selected));
		else {
			center.snap(segmentCenter(selected));
			for (var i = 0; i < selectedness.size(); i++) selectedness.get(i).snap(i == index ? 1 : 0);
		}
	}
	@Override
	public void setBounds(UiBounds bounds) {
		super.setBounds(bounds);
		center.snap(segmentCenter(selected));
	}
	@Override
	public void render(UiContext ctx) {
		if (!visible || labels.isEmpty()) return;
		var painter = ctx.painter;
		var accent = ctx.accent();
		var n = labels.size();
		center.update(ctx.delta);
		for (var tween : selectedness) tween.update(ctx.delta);
		var hover = hovered(ctx) ? indexAt(ctx.mouseX) : -1;
		var half = bounds().width() / (2F * n);
		var velocity = center.velocity();
		var stretch = Math.min(4F, (float) Math.abs(velocity) * 0.02F);
		var c = center.get();
		var left = c - half - (velocity < 0 ? stretch : 0);
		var right = c + half + (velocity > 0 ? stretch : 0);
		var pl = Math.max(bounds().x(), (int) Math.round(left));
		var pr = Math.min(bounds().right(), (int) Math.round(right));
		for (var i = 0; i < n; i++) {
			var x0 = segmentLeft(i, n);
			var segRight = i < n - 1 ? segmentLeft(i + 1, n) + 1 : segmentLeft(n, n);
			if (x0 < pl || segRight > pr + 1) {
				var pressed = i == pressedIndex && down && i != selected;
				var face = pressed ? UiTheme.SECONDARY_PRESSED : i == hover ? UiTheme.SECONDARY_HOVER : UiTheme.SECONDARY_FACE;
				painter.bevel(
					new UiBounds(x0, bounds().y(), segRight - x0, bounds().height()),
					UiTheme.WINDOW_EDGE,
					face,
					UiTheme.SECONDARY_TOP,
					UiTheme.SECONDARY_SHINE,
					UiTheme.SECONDARY_LIP,
					2,
					pressed
				);
			}
		}
		if (pr > pl) painter.bevel(
			new UiBounds(pl, bounds().y(), pr - pl, bounds().height()),
			UiTheme.WINDOW_EDGE,
			accent.strong(),
			accent.light(),
			0xFFF7F7F9,
			accent.darkest(),
			2,
			true
		);
		for (var i = 0; i < n; i++) {
			var x0 = segmentLeft(i, n);
			var x1 = segmentLeft(i + 1, n);
			var sel = selectedness.get(i).value();
			var pressed = i == pressedIndex && down;
			var sink = pressed && i != selected ? 2 : Math.round(2F * sel);
			var text = painter.trim(labels.get(i).getString(), x1 - x0 - 4);
			painter.text(
				text,
				x0 + (x1 - x0 - painter.textWidth(text)) / 2,
				bounds().y() + 1 + (bounds().height() - 2 - 2 - 8 + 1) / 2 + sink,
				lerpInk(accent.ink(), sel)
			);
		}
	}
	private int segmentLeft(int index, int count) {
		return bounds().x() + bounds().width() * index / count;
	}
	private float segmentCenter(int index) {
		var n = labels.size();
		var x0 = segmentLeft(index, n);
		var x1 = segmentLeft(index + 1, n);
		return (float) x0 + (float) (x1 - x0) / 2F;
	}
	private static int lerpInk(int to, double amount) {
		var from = UiTheme.TITLE_INK;
		var t = (float) amount;
		var r = Math.round((from >> 16 & 0xFF) + ((to >> 16 & 0xFF) - (from >> 16 & 0xFF)) * t);
		var g = Math.round((from >> 8 & 0xFF) + ((to >> 8 & 0xFF) - (from >> 8 & 0xFF)) * t);
		var b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
		return from & 0xFF000000 | r << 16 | g << 8 | b;
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
		if (index >= 0 && index != selected) {
			UiSound.click();
			onSelect.accept(index);
		}
		return true;
	}
	@Override
	public boolean keyPressed(UiContext ctx, int keyCode, int scanCode, int modifiers) {
		if (!focusable() || !ctx.focus.is(this) || labels.isEmpty()) return false;
		Integer next = null;
		if (keyCode == 263) next = Math.max(0, selected - 1);
		if (keyCode == 262) next = Math.min(labels.size() - 1, selected + 1);
		if (keyCode == 257 || keyCode == 32) next = selected;
		if (next == null) return false;
		if (next != selected) onSelect.accept(next);
		UiSound.click();
		return true;
	}
	private int indexAt(double mouseX) {
		var width = Math.max(1, bounds().width());
		return (int) Mth.clamp((mouseX - bounds().x()) * labels.size() / width, 0, labels.size() - 1);
	}
}
