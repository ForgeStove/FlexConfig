package io.github.forgestove.flexconfig.client.ui.widget;
import io.github.forgestove.flexconfig.client.ui.core.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;
/** 带过滤框的弹层下拉。弹层由界面统一放到内容层之上，避免被列表 scissor 截断。 */
public class UiDropdown extends UiControl implements UiPopup {
	private static final int ROW_HEIGHT = 16, MAX_ROWS = 8, PAD = 4, FILTER_HEIGHT = 16;
	public record Option(Object value, Component label) {}
	private final List<Option> options;
	private final Supplier<Object> selected;
	private final Consumer<Object> onSelect;
	private final Consumer<UiDropdown> opener;
	private final UiTextField filter = new UiTextField();
	private boolean open, closing;
	private final UiTween openAnim = new UiTween(0);
	private int highlight;
	private int pressedOption = -1;
	private UiBounds popup = UiBounds.EMPTY;
	private UiBounds viewport = UiBounds.EMPTY;
	public UiDropdown(List<Option> options, Supplier<Object> selected, Consumer<Object> onSelect, Consumer<UiDropdown> opener) {
		this.options = options;
		this.selected = selected;
		this.onSelect = onSelect;
		this.opener = opener;
	}
	public UiTextField filterField() {
		return filter;
	}
	public void setViewport(UiBounds viewport) {
		this.viewport = viewport;
	}
	public void setOpen(boolean open) {
		this.open = open;
		if (open) {
			closing = false;
			filter.setValue("");
			openAnim.start(0, 1, UiMotion.POPUP_IN);
		} else openAnim.snap(0);
		highlight = Math.max(0, filtered().indexOf(currentOption()));
	}
	@Override
	public void close() {
		open = false;
		closing = false;
	}
	public void beginClose() {
		if (!open) return;
		open = false;
		closing = true;
		openAnim.start(1, 0, UiMotion.POPUP_OUT);
	}
	@Override
	public boolean closing() {
		return closing;
	}
	private @Nullable Option currentOption() {
		var value = selected.get();
		for (var option : options) if (Objects.equals(option.value(), value)) return option;
		return null;
	}
	private List<Option> filtered() {
		var query = filter.value().trim().toLowerCase();
		if (query.isEmpty()) return options;
		var result = new ArrayList<Option>();
		for (var option : options) if (option.label().getString().toLowerCase().contains(query)) result.add(option);
		return result;
	}
	@Override
	public void render(UiContext ctx) {
		if (!visible) return;
		var pressed = open || down;
		var face = !enabled ? 0xFF6E6A5C : pressed ? UiTheme.SECONDARY_PRESSED : hovered(ctx) ? UiTheme.SECONDARY_HOVER : UiTheme.SECONDARY_FACE;
		var inner = ctx.painter.bevel(
			bounds(),
			open ? UiTheme.OUTLINE : UiTheme.WINDOW_EDGE,
			face,
			UiTheme.SECONDARY_TOP,
			UiTheme.SECONDARY_SHINE,
			UiTheme.SECONDARY_LIP,
			2,
			pressed
		);
		var current = currentOption();
		var label = current == null ? Component.empty() : current.label();
		var text = ctx.painter.trim(label.getString(), bounds().width() - 20);
		var ink = enabled ? UiTheme.TITLE_INK : UiTheme.DISABLED;
		ctx.painter.text(text, bounds().x() + 5, inner.y() + (inner.height() - 7) / 2, ink);
		ctx.painter.chevron(bounds().right() - 12, inner.y() + (inner.height() - 4) / 2, ink);
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
		opener.accept(this);
		return true;
	}
	private void layoutPopup() {
		var list = filtered();
		var rows = UiMath.clamp(list.size(), 1, MAX_ROWS);
		var width = Math.max(bounds().width(), 140);
		var height = FILTER_HEIGHT + PAD * 2 + rows * ROW_HEIGHT + PAD;
		var below = viewport.bottom() - bounds().bottom();
		var y = below >= height ? bounds().bottom() + 1 : Math.max(viewport.y(), bounds().y() - height - 1);
		popup = new UiBounds(bounds().x(), y, width, height);
	}
	@Override
	public void renderPopup(UiContext ctx) {
		if (!open && !closing) return;
		layoutPopup();
		openAnim.update(ctx.delta);
		if (closing && openAnim.done()) {
			closing = false;
			return;
		}
		var t = openAnim.value();
		var drawn = new UiBounds(popup.x(), popup.y() + popup.height() - Math.round(popup.height() * (0.92F + 0.08F * t)), popup.width(), Math.round(popup.height() * (0.92F + 0.08F * t)));
		filter.setBounds(new UiBounds(drawn.x() + PAD, drawn.y() + PAD, drawn.width() - PAD * 2, FILTER_HEIGHT));
		ctx.painter.pushZ(200);
		ctx.painter.pushAlpha(t);
		ctx.painter.border(drawn, UiTheme.WINDOW_EDGE);
		ctx.painter.rect(drawn.x() + 1, drawn.y() + 1, drawn.width() - 2, 1, UiTheme.GROOVE_LIGHT);
		ctx.painter.rect(drawn.x() + 1, drawn.y() + 2, drawn.width() - 2, drawn.height() - 3, UiTheme.SIDEBAR);
		filter.render(ctx);
		var list = filtered();
		var visible = (drawn.height() - FILTER_HEIGHT - PAD * 3) / ROW_HEIGHT;
		var start = UiMath.clamp(highlight - visible + 1, 0, Math.max(0, list.size() - visible));
		var y = drawn.y() + PAD + FILTER_HEIGHT + PAD;
		for (var i = 0; i < visible && start + i < list.size(); i++) {
			var index = start + i;
			var row = new UiBounds(drawn.x() + PAD, y + i * ROW_HEIGHT, drawn.width() - PAD * 2, ROW_HEIGHT);
			if (index == highlight) ctx.painter.rect(row, UiTheme.ROW_HOVER);
			if (Objects.equals(list.get(index).value(), selected.get())) ctx.painter.rect(row.x(), row.y() + 2, 2, ROW_HEIGHT - 4, ctx.accent().base());
			ctx.painter.text(
				ctx.painter.trim(list.get(index).label().getString(), row.width() - 10),
				row.x() + 6,
				row.y() + (ROW_HEIGHT - ctx.painter.lineHeight()) / 2,
				index == highlight ? UiTheme.TEXT : UiTheme.TEXT_2
			);
		}
		ctx.painter.popAlpha();
		ctx.painter.popZ();
	}
	@Override
	public boolean isOverPopup(double mouseX, double mouseY) {
		return open && popup.contains(mouseX, mouseY);
	}
	@Override
	public boolean popupMouseClicked(UiContext ctx, double mouseX, double mouseY, int button) {
		if (!isOverPopup(mouseX, mouseY)) return false;
		if (button != 0) return true;
		if (filter.bounds().contains(mouseX, mouseY)) return filter.mouseClicked(ctx, mouseX, mouseY, button);
		var list = filtered();
		var visible = (popup.height() - FILTER_HEIGHT - PAD * 3) / ROW_HEIGHT;
		var start = UiMath.clamp(highlight - visible + 1, 0, Math.max(0, list.size() - visible));
		var startY = popup.y() + PAD + FILTER_HEIGHT + PAD;
		var index = (int) ((mouseY - startY) / ROW_HEIGHT);
		pressedOption = index >= 0 && index < visible && start + index < list.size() ? start + index : -1;
		return true;
	}
	@Override
	public boolean popupMouseReleased(UiContext ctx, double mouseX, double mouseY, int button) {
		if (button != 0) return false;
		var index = pressedOption;
		pressedOption = -1;
		if (index < 0) return filter.mouseReleased(ctx, mouseX, mouseY, button);
		var list = filtered();
		if (index < list.size() && isOverPopup(mouseX, mouseY)) select(list.get(index).value());
		return true;
	}
	private void select(Object value) {
		UiSound.click();
		onSelect.accept(value);
		close();
	}
	@Override
	public boolean popupMouseScrolled(double amount) {
		var list = filtered();
		highlight = UiMath.clamp(highlight - (int) Math.signum(amount), 0, Math.max(0, list.size() - 1));
		return true;
	}
	@Override
	public boolean popupKeyPressed(UiContext ctx, int keyCode, int scanCode, int modifiers) {
		if (!open) return false;
		var list = filtered();
		if (keyCode == 256) {   // Esc
			close();
			return true;
		}
		if (keyCode == 265) {   // Up
			highlight = Math.max(0, highlight - 1);
			return true;
		}
		if (keyCode == 264) {   // Down
			highlight = UiMath.clamp(highlight + 1, 0, Math.max(0, list.size() - 1));
			return true;
		}
		if (keyCode == 257 || keyCode == 335) {   // Enter
			if (!list.isEmpty() && highlight >= 0 && highlight < list.size()) select(list.get(highlight).value());
			else close();
			return true;
		}
		return filter.keyPressed(ctx, keyCode, scanCode, modifiers);
	}
	@Override
	public boolean popupCharTyped(UiContext ctx, char codePoint) {
		if (!open) return false;
		highlight = 0;
		return filter.charTyped(ctx, codePoint);
	}
}
