package io.github.forgestove.flexconfig.client.ui.widget;
import io.github.forgestove.flexconfig.client.ui.core.*;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.*;
public class UiSlider extends UiControl {
	private final double min, max, step;
	private final DoubleSupplier value;
	private final DoubleConsumer onDrag;
	private final Runnable onCommit;
	private final DoubleFunction<String> format;
	private final UiAnim hover = new UiAnim(0);
	private final UiAnim thumb = new UiAnim(0, UiMotion.THUMB);
	private boolean dragging, thumbInit;
	private double dragStart;
	private long lastClickMs;
	@Nullable private UiTextField editor;
	public UiSlider(double min, double max, DoubleSupplier value, DoubleConsumer onDrag, Runnable onCommit, DoubleFunction<String> format) {
		this(min, max, Math.max(0.01, (max - min) / 100), value, onDrag, onCommit, format);
	}
	public UiSlider(double min, double max, double step, DoubleSupplier value, DoubleConsumer onDrag, Runnable onCommit, DoubleFunction<String> format) {
		this.min = min;
		this.max = max;
		this.step = step <= 0 ? 1 : step;
		this.value = value;
		this.onDrag = onDrag;
		this.onCommit = onCommit;
		this.format = format;
	}
	@Override
	public void render(UiContext ctx) {
		if (!visible) return;
		hover.target(focusable() && hovered(ctx) ? 1 : 0);
		hover.update(ctx.delta);
		var painter = ctx.painter;
		var accent = ctx.accent();
		var track = trackBounds();
		var progress = progress(value.getAsDouble());
		painter.rect(track, UiTheme.WINDOW_EDGE);
		var fillX = track.x() + 1 + (int) Math.round((track.width() - 2) * progress);
		trackPart(painter, track.x() + 1, fillX, track.y(), accent.bright(), enabled ? accent.strong() : UiTheme.OFF_FILL);
		trackPart(painter, fillX, track.right() - 1, track.y(), UiTheme.TRACK_LIGHT, UiTheme.OFF_FILL);
		var steps = (int) Math.round((max - min) / step);
		var tickSteps = steps > 1 && steps <= 24 ? steps : 0;
		for (var i = 1; i < tickSteps; i++) {
			var tickX = track.x() + 1 + Math.round((track.width() - 2) * (float) i / tickSteps);
			painter.rect(tickX, track.y() + 2, 1, 3, tickX < fillX ? accent.darkest() : UiTheme.GROOVE_LIGHT);
		}
		thumb.target(fillX);
		if (dragging) thumb.snap(fillX);
		else if (!thumbInit) {
			thumb.jumpTo(fillX);
			thumbInit = true;
		} else thumb.update(ctx.delta);
		var knobX = (int) Math.round(thumb.get()) - 5;
		painter.bevel(
			new UiBounds(knobX, bounds().y() + (bounds().height() - 12) / 2, 10, 12),
			UiTheme.WINDOW_EDGE,
			!enabled ? UiTheme.KNOB_LO : dragging || hover.get() > 0.5 ? UiTheme.KNOB_HI : UiTheme.KNOB,
			UiTheme.KNOB_HI,
			UiTheme.KNOB_HI,
			UiTheme.KNOB_LO,
			2,
			dragging
		);
		var valueCell = new UiBounds(bounds().right() - 46, bounds().y() + (bounds().height() - UiTheme.CONTROL_HEIGHT) / 2, 46, UiTheme.CONTROL_HEIGHT);
		if (editor != null) {
			editor.setBounds(valueCell);
			editor.render(ctx);
		} else {
			var text = painter.trim(format.apply(value.getAsDouble()), 44);
			var color = !enabled ? UiTheme.DISABLED : dragging ? accent.light() : UiTheme.TEXT_2;
			painter.textRight(Component.literal(text), bounds().right(), bounds().y() + (bounds().height() - painter.lineHeight()) / 2, color);
		}
		if (dragging) {
			var text = format.apply(value.getAsDouble());
			var boxW = painter.textWidth(text) + 8;
			var boxH = painter.lineHeight() + 7;
			var boxX = UiMath.clamp(knobX + 5 - boxW / 2, bounds().x(), bounds().right() - boxW);
			var boxY = bounds().y() - boxH - 3;
			painter.border(boxX, boxY, boxW, boxH, UiTheme.WINDOW_EDGE);
			painter.rect(boxX + 1, boxY + 1, boxW - 2, boxH - 2, UiTheme.SIDEBAR);
			painter.text(text, boxX + 4, boxY + 3, UiTheme.TEXT);
		}
	}
	private UiBounds trackBounds() {
		var width = Math.max(1, bounds().width() - 46);
		return new UiBounds(bounds().x(), bounds().y() + (bounds().height() - 6) / 2, width, 6);
	}
	private double progress(double raw) {
		var span = max - min;
		return span <= 0 ? 0 : UiMath.clamp((raw - min) / span, 0, 1);
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
		ctx.setDragOwner(this);
		dragTo(mouseX);
		return true;
	}
	private UiBounds valueCell() {
		return new UiBounds(bounds().right() - 46, bounds().y() + (bounds().height() - UiTheme.CONTROL_HEIGHT) / 2, 46, UiTheme.CONTROL_HEIGHT);
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
			onDrag.accept(UiMath.clamp(parsed, min, max));
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
		dragTo(mouseX);
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
		var step = (max - min) / 100;
		if (keyCode == 263) {   // left
			onDrag.accept(UiMath.clamp(value.getAsDouble() - step, min, max));
			onCommit.run();
			return true;
		}
		if (keyCode == 262) {   // right
			onDrag.accept(UiMath.clamp(value.getAsDouble() + step, min, max));
			onCommit.run();
			return true;
		}
		return false;
	}
	private static void trackPart(UiPainter painter, int from, int to, int ty, int top, int face) {
		painter.rect(from, ty + 1, Math.max(0, to - from), 1, top);
		painter.rect(from, ty + 2, Math.max(0, to - from), 3, face);
	}
	private void dragTo(double mouseX) {
		var track = trackBounds();
		var usable = Math.max(1, track.width() - 2);
		var progress = UiMath.clamp((mouseX - track.x() - 1) / usable, 0, 1);
		onDrag.accept(min + (max - min) * progress);
	}
}
