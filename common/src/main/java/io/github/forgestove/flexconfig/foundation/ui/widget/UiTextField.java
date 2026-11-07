package io.github.forgestove.flexconfig.foundation.ui.widget;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.forgestove.flexconfig.foundation.client.ClientUtil;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.function.*;
/** 自绘单行文本编辑：光标、选区、横向滚动和剪贴板都自己处理。 */
public class UiTextField extends UiControl {
	private static final int PAD = 3;
	private String value = "";
	private int cursor;
	private int anchor = -1;
	private int maxLength = Integer.MAX_VALUE;
	private Predicate<String> filter = ignored -> true;
	private Consumer<String> responder = ignored -> {};
	private @Nullable Component placeholder;
	private @Nullable Runnable onEnter;
	private boolean error;
	private boolean leadingSearch;
	private int scroll;
	private boolean dragging;
	private boolean focusedLast;
	private long caretShownAt;
	private long lastClickMs;
	private final Deque<String> history = new ArrayDeque<>();
	public UiTextField() {}
	public UiTextField(String initial) {
		value = initial;
		cursor = value.length();
	}
	public String value() {
		return value;
	}
	/** 外部同步值：不触发 responder，光标放到末尾并清除选区。 */
	public void setValue(String newValue) {
		var next = newValue == null ? "" : newValue;
		if (next.equals(value)) return;
		value = next;
		cursor = value.length();
		anchor = -1;
		scroll = 0;
		history.clear();
	}
	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength;
	}
	public void setFilter(Predicate<String> filter) {
		this.filter = filter;
	}
	public void setResponder(Consumer<String> responder) {
		this.responder = responder;
	}
	public void setPlaceholder(@Nullable Component placeholder) {
		this.placeholder = placeholder;
	}
	public void setError(boolean error) {
		this.error = error;
	}
	public void setLeadingSearch(boolean leadingSearch) {
		this.leadingSearch = leadingSearch;
	}
	public void setOnEnter(@Nullable Runnable onEnter) {
		this.onEnter = onEnter;
	}
	private int textLeft() {
		return PAD + (leadingSearch ? 11 : 0);
	}
	public boolean isFocused(UiContext ctx) {
		return ctx.focus.is(this);
	}
	@Override
	public void render(UiContext ctx) {
		if (!visible) return;
		clampScroll();
		var focused = isFocused(ctx);
		if (focused && !focusedLast) caretShownAt = Util.getMillis();   // 聚焦后光标从可见相开始闪
		focusedLast = focused;
		var painter = ctx.painter;
		if (error) {
			painter.rect(bounds(), UiTheme.DANGER);
			painter.rect(bounds().inset(1, 1), UiTheme.FIELD);
		} else painter.field(bounds(), isFocused(ctx), hovered(ctx), ctx.accent().base());
		var inner = bounds().inset(1, 1);
		if (leadingSearch) {
			var lx = inner.x() + 3;
			var ly = inner.y() + (inner.height() - 7) / 2;
			var lens = isFocused(ctx) ? UiTheme.TEXT : UiTheme.TEXT_3;
			painter.rect(lx + 1, ly + 1, 3, 1, lens);
			painter.rect(lx + 1, ly + 5, 3, 1, lens);
			painter.rect(lx, ly + 2, 1, 3, lens);
			painter.rect(lx + 4, ly + 2, 1, 3, lens);
			painter.rect(lx + 4, ly + 5, 2, 1, lens);
			painter.rect(lx + 5, ly + 6, 1, 1, lens);
		}
		var textX = inner.x() + textLeft() - scroll;
		var textY = bounds().y() + (bounds().height() - painter.lineHeight()) / 2 + 1;
		painter.scissorOn(inner);
		if (value.isEmpty()) {
			if (placeholder != null) painter.text(placeholder, inner.x() + textLeft(), textY, UiTheme.TEXT_3);
		} else {
			var selectionStart = selectionStart();
			var selectionEnd = selectionEnd();
			if (selectionStart >= 0 && selectionEnd > selectionStart) {
				var from = painter.textWidth(value.substring(0, selectionStart));
				var to = painter.textWidth(value.substring(0, selectionEnd));
				painter.rect(textX + from, textY - 1, to - from, painter.lineHeight() + 1, UiTheme.mulAlpha(ctx.accent().base(), 0.45F));
			}
			painter.text(value, textX, textY, enabled ? error ? UiTheme.DANGER_LIGHT : UiTheme.TEXT : UiTheme.DISABLED);
		}
		if (isFocused(ctx) && (Util.getMillis() - caretShownAt) % 1060 < 530) {
			var caret = textX + painter.textWidth(value.substring(0, cursor));
			painter.rect(caret, textY - 1, 1, painter.lineHeight() + 1, UiTheme.TEXT);
		}
		painter.scissorOff();
		showTooltip(ctx);
	}
	@Override
	public boolean mouseClicked(UiContext ctx, double mouseX, double mouseY, int button) {
		if (button != 0 || !focusable() || !hovered(ctx)) return false;
		ctx.focus.set(this);
		var previous = cursor;
		var now = Util.getMillis();
		if (now - lastClickMs < 300) {   // 双击全选
			lastClickMs = 0;
			anchor = 0;
			cursor = value.length();
		} else {
			cursor = indexAt(mouseX);
			anchor = Screen.hasShiftDown() ? previous : cursor;
		}
		lastClickMs = now;
		caretShownAt = now;
		dragging = true;
		ctx.setDragOwner(this);
		clampScroll();
		return true;
	}
	@Override
	public boolean mouseDragged(UiContext ctx, double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (!dragging || button != 0) return false;
		cursor = indexAt(mouseX);
		caretShownAt = Util.getMillis();
		clampScroll();
		return true;
	}
	@Override
	public boolean mouseReleased(UiContext ctx, double mouseX, double mouseY, int button) {
		if (!dragging) return false;
		dragging = false;
		return true;
	}
	@Override
	public boolean keyPressed(UiContext ctx, int keyCode, int scanCode, int modifiers) {
		if (!isFocused(ctx)) return false;
		var control = Screen.hasControlDown();
		switch (keyCode) {
			case InputConstants.KEY_LEFT -> moveCaret(-1, control);
			case InputConstants.KEY_RIGHT -> moveCaret(1, control);
			case InputConstants.KEY_HOME -> moveTo(0);
			case InputConstants.KEY_END -> moveTo(value.length());
			case InputConstants.KEY_BACKSPACE -> delete(-1, control);
			case InputConstants.KEY_DELETE -> delete(1, control);
			case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
				if (onEnter != null) onEnter.run();
			}
			case GLFW.GLFW_KEY_ESCAPE -> {
				anchor = -1;
				ctx.focus.clear();
			}
			default -> {
				if (!control) return false;
				switch (keyCode) {
					case 65 -> {   // A
						anchor = 0;
						cursor = value.length();
					}
					case 67 -> copy();
					case 88 -> cut();
					case 86 -> paste();
					case 90 -> {
						undo();
						return true;
					}
					default -> {
						return false;
					}
				}
			}
		}
		caretShownAt = Util.getMillis();
		clampScroll();
		return true;
	}
	@Override
	public boolean charTyped(UiContext ctx, char codePoint) {
		if (!isFocused(ctx)) return false;
		if (Character.isISOControl(codePoint)) return false;
		insert(String.valueOf(codePoint));
		clampScroll();
		return true;
	}
	private void insert(String text) {
		var start = selectionStart();
		var end = selectionEnd();
		var base = start >= 0 ? value.substring(0, start) + text + value.substring(end) : value.substring(0, cursor) + text + value.substring(cursor);
		if (base.length() > maxLength || !filter.test(base)) return;
		record();
		value = base;
		cursor = (start >= 0 ? start : cursor) + text.length();
		anchor = -1;
		changed();
	}
	private void delete(int direction, boolean word) {
		var start = selectionStart();
		var end = selectionEnd();
		if (start >= 0 && end > start) {
			record();
			value = value.substring(0, start) + value.substring(end);
			cursor = start;
			anchor = -1;
			changed();
			return;
		}
		if (direction < 0) {
			if (cursor <= 0) return;
			var from = word ? wordLeft() : cursor - Character.charCount(value.codePointBefore(cursor));
			record();
			value = value.substring(0, from) + value.substring(cursor);
			cursor = from;
		} else {
			if (cursor >= value.length()) return;
			var to = word ? wordRight() : cursor + Character.charCount(value.codePointAt(cursor));
			record();
			value = value.substring(0, cursor) + value.substring(to);
		}
		anchor = -1;
		changed();
	}
	private void moveTo(int target) {
		var extend = Screen.hasShiftDown();
		var previous = cursor;
		cursor = target;
		if (extend) {
			if (anchor < 0) anchor = previous;
		} else anchor = -1;
	}
	private void moveCaret(int direction, boolean word) {
		var extend = Screen.hasShiftDown();
		if (!extend && anchor >= 0 && anchor != cursor) {   // 有选区时先收起到对应一端
			cursor = direction < 0 ? selectionStart() : selectionEnd();
			anchor = -1;
			return;
		}
		var previous = cursor;
		if (word) cursor = direction < 0 ? wordLeft() : wordRight();
		else if (direction < 0) {
			if (cursor > 0) cursor -= Character.charCount(value.codePointBefore(cursor));
		} else if (cursor < value.length()) cursor += Character.charCount(value.codePointAt(cursor));
		if (extend) {
			if (anchor < 0) anchor = previous;
		} else anchor = -1;
	}
	private int wordLeft() {
		var i = cursor;
		while (i > 0 && Character.isWhitespace(value.charAt(i - 1))) i--;
		while (i > 0 && !Character.isWhitespace(value.charAt(i - 1))) i--;
		return i;
	}
	private int wordRight() {
		var i = cursor;
		while (i < value.length() && !Character.isWhitespace(value.charAt(i))) i++;
		while (i < value.length() && Character.isWhitespace(value.charAt(i))) i++;
		return i;
	}
	private void record() {
		history.push(value);
		while (history.size() > 50) history.removeLast();
	}
	private void undo() {
		if (history.isEmpty()) return;
		value = history.pop();
		cursor = Math.min(cursor, value.length());
		anchor = cursor;
		changed();
	}
	private void copy() {
		var start = selectionStart();
		if (start < 0) return;
		ClientUtil.mc.keyboardHandler.setClipboard(value.substring(start, selectionEnd()));
	}
	private void cut() {
		copy();
		delete(1, false);
	}
	private void paste() {
		var clipboard = ClientUtil.mc.keyboardHandler.getClipboard();
		if (clipboard.isEmpty()) return;
		insert(clipboard.replaceAll("[\\r\\n\\t]+", " "));
	}
	private void changed() {
		caretShownAt = Util.getMillis();
		responder.accept(value);
	}
	private int selectionStart() {
		return anchor < 0 ? -1 : Math.min(cursor, anchor);
	}
	private int selectionEnd() {
		return anchor < 0 ? -1 : Math.max(cursor, anchor);
	}
	private int indexAt(double mouseX) {
		var innerX = bounds().x() + 1 + textLeft() - scroll;
		var target = mouseX - innerX;
		var index = 0;
		var width = 0;
		while (index < value.length()) {
			var next = index + Character.charCount(value.codePointAt(index));
			var piece = ClientUtil.mc.font.width(value.substring(index, next));
			if (width + piece / 2.0 > target) return index;
			width += piece;
			index = next;
		}
		return value.length();
	}
	private void clampScroll() {
		var innerWidth = bounds().width() - 2 - PAD * 2;
		if (innerWidth <= 0) return;
		var caret = ClientUtil.mc.font.width(value.substring(0, cursor));
		if (caret - scroll > innerWidth) scroll = caret - innerWidth;
		else if (caret - scroll < 0) scroll = caret;
		var total = ClientUtil.mc.font.width(value);
		scroll = Mth.clamp(scroll, 0, Math.max(0, total - innerWidth));
	}
}
