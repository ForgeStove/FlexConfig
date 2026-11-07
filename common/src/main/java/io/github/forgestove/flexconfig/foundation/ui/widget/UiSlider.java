package io.github.forgestove.flexconfig.foundation.ui.widget;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.*;
public class UiSlider extends UiControl {
	private final double min, max, step;
	private final int decimals;
	private final DoubleSupplier value;
	private final DoubleConsumer onDrag;
	private final Runnable onCommit;
	private final DoubleFunction<String> format;
	private @Nullable DoubleSupplier defaultValue;
	private @Nullable UiBounds valueCell;
	private final UiAnim hover = new UiAnim(0);
	private final UiAnim thumb = new UiAnim(0, UiMotion.THUMB);
	private final UiTween changedTint = new UiTween(0);
	private boolean dragging, thumbInit;
	private double dragStart, virtualX, lastPointerX;
	private long lastClickMs;
	@Nullable private UiTextField editor;
	public UiSlider(double min, double max, double step, DoubleSupplier value, DoubleConsumer onDrag, Runnable onCommit, DoubleFunction<String> format) {
		this.min = min;
		this.max = max;
		this.step = step <= 0 ? 1 : step;
		int value1 = Math.max(decimalPlaces(this.step), decimalPlaces(min));
		decimals = Mth.clamp(value1, 0, 8);
		this.value = value;
		this.onDrag = onDrag;
		this.onCommit = onCommit;
		this.format = format;
	}
	public void defaultValue(DoubleSupplier defaultValue) {
		this.defaultValue = defaultValue;
	}
	@Override
	public void render(UiContext ctx) {
		if (!visible) return;
		hover.target(focusable() && hovered(ctx) ? 1 : 0);
		hover.update(ctx.delta);
		var tintTarget = isDefault() ? 0F : 1F;
		if (changedTint.target() != tintTarget) changedTint.start(tintTarget, UiMotion.TOOLTIP_FADE);
		changedTint.update(ctx.delta);
		var painter = ctx.painter;
		var accent = ctx.accent();
		var track = trackBounds();
		var progress = progress(value.getAsDouble());
		var valueX = track.x() + 1 + (int) Math.round((track.width() - 2) * progress);
		var fillX = dragging ? (int) Math.round(thumb.get()) : valueX;
		painter.rect(track, UiTheme.WINDOW_EDGE);
		trackPart(painter, track.x() + 1, fillX, track.y(),
			enabled ? accent.light() : 0xFFA3A3A6,
			enabled ? accent.base() : 0xFF8C8D90,
			enabled ? accent.lightest() : 0xFF97989B);
		trackPart(painter, fillX, track.right() - 1, track.y(), 0xFFA3A3A6, 0xFF8C8D90, 0xFF97989B);
		var steps = (int) Math.round((max - min) / step);
		var tickSteps = steps > 1 && steps <= 24 ? steps : 0;
		for (var i = 1; i < tickSteps; i++) {
			var tickX = track.x() + 1 + Math.round((track.width() - 2) * (float) i / tickSteps);
			painter.rect(tickX, track.y() + 1, 1, 4, tickX < fillX ? accent.darkest() : UiTheme.GROOVE_LIGHT);
		}
		if (defaultValue != null) {
			double value1 = defaultValue.getAsDouble();
			var dx = track.x() + 1 + (int) Math.round((track.width() - 2) * progress(Mth.clamp(value1, min, max)));
			painter.rect(dx, track.y() - 4, 1, 3, accent.base());
		}
		thumb.target(valueX);
		if (!dragging && !thumbInit) {
			thumb.jumpTo(valueX);
			thumbInit = true;
		} else if (!dragging) thumb.update(ctx.delta);
		var knobX = (int) Math.round(thumb.get()) - 5;
		var knobFace = !enabled ? 0xFFD0D1D4 : dragging ? 0xFFB1B2B5 : hover.get() > 0.5 ? 0xFFF4F6F9 : 0xFFD0D1D4;
		painter.bevel(
			new UiBounds(knobX, bounds().y() + (bounds().height() - 12) / 2 - 1, 10, 12),
			enabled ? UiTheme.WINDOW_EDGE : 0xFF8C8D90,
			knobFace,
			0xFFECECED,
			0xFFE2E3E5,
			enabled ? 0xFF58585A : 0xFFB1B2B5,
			2,
			dragging
		);
		var valueCell = valueCell();
		if (editor != null) {
			editor.setBounds(new UiBounds(valueCell.x() - 4, valueCell.y() - 4, valueCell.width() + 8, 16));
			editor.render(ctx);
		} else {
			var text = painter.trim(format.apply(value.getAsDouble()), valueCell.width() - 2);
			var idle = hover.get() > 0.5 ? UiTheme.TEXT : UiTheme.TEXT_2;
			var color = !enabled ? UiTheme.DISABLED : UiTheme.fadeToward(accent.light(), idle, changedTint.value());
			painter.textRight(Component.literal(text), valueCell.right(), valueCell.y(), color);
		}
		if (dragging) {
			var text = format.apply(value.getAsDouble());
			var boxW = painter.textWidth(text) + 8;
			var boxH = painter.lineHeight() + 7;
			var boxX = Mth.clamp(knobX + 5 - boxW / 2, bounds().x(), bounds().right() - boxW);
			var boxY = bounds().y() - boxH - 3;
			painter.border(boxX, boxY, boxW, boxH, UiTheme.WINDOW_EDGE);
			painter.rect(boxX + 1, boxY + 1, boxW - 2, boxH - 2, UiTheme.SIDEBAR);
			painter.text(text, boxX + 4, boxY + 3, UiTheme.TEXT);
		}
		showTooltip(ctx);
	}
	private UiBounds trackBounds() {
		var width = Math.max(1, bounds().width() - (valueCell == null ? 46 : 0));
		return new UiBounds(bounds().x(), bounds().y() + (bounds().height() - 6) / 2, width, 6);
	}
	private double progress(double raw) {
		var span = max - min;
		return span <= 0 ? 0 : Mth.clamp((raw - min) / span, 0, 1);
	}
	private boolean isDefault() {
		return defaultValue == null || Math.abs(value.getAsDouble() - defaultValue.getAsDouble()) < step / 1000.0;
	}
	@Override
	public boolean mouseClicked(UiContext ctx, double mouseX, double mouseY, int button) {
		if (!focusable() || !hovered(ctx)) return false;
		if (editor != null) {
			if (editor.bounds().contains(mouseX, mouseY)) return editor.mouseClicked(ctx, mouseX, mouseY, button);
			closeEditor();
			return true;
		}
		var now = Util.getMillis();
		if (button == 0 && valueCell().contains(mouseX, mouseY) && now - lastClickMs < 300) {
			lastClickMs = 0;
			openEditor();
			ctx.focus.set(editor);
			return true;
		}
		lastClickMs = now;
		if (button != 0) return false;
		ctx.focus.set(this);
		dragging = true;
		dragStart = value.getAsDouble();
		lastPointerX = mouseX;
		var track = trackBounds();
		var thumbX = thumb.get();
		virtualX = Math.abs(mouseX - thumbX) <= 6 ? thumbX : Mth.clamp(mouseX, track.x() + 1, track.right() - 1);
		applyDrag();
		ctx.setDragOwner(this);
		return true;
	}
	public void setValueCell(@Nullable UiBounds cell) {
		valueCell = cell;
	}
	private UiBounds valueCell() {
		return valueCell != null ? valueCell : new UiBounds(bounds().right() - 46, bounds().y() + (bounds().height() - UiTheme.CONTROL_HEIGHT) / 2, 46, UiTheme.CONTROL_HEIGHT);
	}
	private void openEditor() {
		editor = new UiTextField(format.apply(value.getAsDouble()).replace("%", ""));
		editor.setFilter(s -> s.matches("-?\\d*(\\.\\d*)?"));
		editor.setMaxLength(24);
		editor.setOnEnter(this::commitEditor);
	}
	private void commitEditor() {
		var field = editor;
		if (field == null) return;
		try {
			var parsed = Double.parseDouble(field.value().trim());
			onDrag.accept(snap(parsed));
			onCommit.run();
			UiSound.click(1.6F);
		} catch (NumberFormatException ignored) {}
		closeEditor();
	}
	private boolean editorKeyPressed(UiContext ctx, int keyCode, int scanCode, int modifiers) {
		var field = editor;
		if (field == null) return false;
		if (!ctx.focus.is(field)) {
			closeEditor();
			return false;
		}
		if (keyCode == 256) {
			closeEditor();
			return true;
		}
		field.keyPressed(ctx, keyCode, scanCode, modifiers);
		return true;
	}
	private void closeEditor() {
		editor = null;
	}
	public @Nullable UiTextField editor() {
		return editor;
	}
	@Override
	public boolean mouseDragged(UiContext ctx, double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (!dragging || button != 0) return false;
		var delta = mouseX - lastPointerX;
		virtualX += Screen.hasShiftDown() ? delta / 10D : delta;
		lastPointerX = mouseX;
		applyDrag();
		return true;
	}
	@Override
	public boolean mouseReleased(UiContext ctx, double mouseX, double mouseY, int button) {
		if (!dragging) return false;
		dragging = false;
		if (value.getAsDouble() != dragStart) UiSound.click(1.6F);
		onCommit.run();
		return true;
	}
	@Override
	public boolean charTyped(UiContext ctx, char codePoint) {
		var field = editor;
		if (field == null) return false;
		if (!ctx.focus.is(field)) {
			closeEditor();
			return false;
		}
		return field.charTyped(ctx, codePoint);
	}
	@Override
	public boolean keyPressed(UiContext ctx, int keyCode, int scanCode, int modifiers) {
		if (editor != null) return editorKeyPressed(ctx, keyCode, scanCode, modifiers);
		if (!focusable() || !ctx.focus.is(this)) return false;
		if (keyCode == 263) {   // left
			double value1 = value.getAsDouble() - step;
			onDrag.accept(Mth.clamp(value1, min, max));
			onCommit.run();
			return true;
		}
		if (keyCode == 262) {   // right
			double value1 = value.getAsDouble() + step;
			onDrag.accept(Mth.clamp(value1, min, max));
			onCommit.run();
			return true;
		}
		return false;
	}
	private static void trackPart(UiPainter painter, int from, int to, int ty, int top, int face, int bottom) {
		var width = Math.max(0, to - from);
		painter.rect(from, ty + 1, width, 1, top);
		painter.rect(from, ty + 2, width, 2, face);
		painter.rect(from, ty + 4, width, 1, bottom);
	}
	private void applyDrag() {
		var track = trackBounds();
		var clamped = Mth.clamp(virtualX, track.x() + 1, track.right() - 1);
		var usable = Math.max(1, track.width() - 2);
		var raw = snap(min + (max - min) * ((clamped - track.x() - 1) / usable));
		if (defaultValue != null) {
			double value1 = defaultValue.getAsDouble();
			var fallback = Mth.clamp(value1, min, max);
			if (Math.abs(clamped - xFor(fallback)) <= 2) raw = fallback;
		}
		thumb.snap(clamped);
		if (raw != value.getAsDouble()) onDrag.accept(raw);
	}
	private double snap(double raw) {
		var clamped = Mth.clamp(raw, min, max);
		var n = Math.round((clamped - min) / step);
		return Mth.clamp(BigDecimal.valueOf(min + n * step).setScale(decimals, RoundingMode.HALF_UP).doubleValue(), min, max);
	}
	private static int decimalPlaces(double value) {
		return Math.max(0, BigDecimal.valueOf(value).stripTrailingZeros().scale());
	}
	private double xFor(double raw) {
		var track = trackBounds();
		return track.x() + 1 + (track.width() - 2) * progress(raw);
	}
}
