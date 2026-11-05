package io.github.forgestove.flexconfig.client.ui.widget;
import io.github.forgestove.flexconfig.client.ClientUtil;
import io.github.forgestove.flexconfig.client.ui.core.*;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.function.IntConsumer;
/** 通用弹层列表：行右键菜单、页面菜单、tab 菜单共用。 */
public class UiPopupMenu implements UiPopup {
	private static final int ROW_HEIGHT = 16, MAX_ROWS = 8;
	private final List<Item> items;
	private final IntConsumer onSelect;
	private final UiTween openAnim = new UiTween(0);
	private final UiAnim highlight = new UiAnim(0, UiMotion.SELECT);
	private UiBounds popup = UiBounds.EMPTY;
	private int highlighted = -1, scrollRows;
	private boolean above;
	private boolean open, closing;
	public UiPopupMenu(List<Item> items, IntConsumer onSelect) {
		this.items = items;
		this.onSelect = onSelect;
	}
	public void place(UiBounds viewport, int x, int y) {
		var font = ClientUtil.mc.font;
		var width = 1;
		for (var item : items) width = Math.max(width, font.width(item.label()) + 27);
		var height = visibleRows() * ROW_HEIGHT + 2;
		var px = UiMath.clamp(x, viewport.x() + 2, Math.max(viewport.x() + 2, viewport.right() - width - 2));
		var py = y + 1;
		above = py + height > viewport.bottom() - 2 && y - height - 1 >= viewport.y();
		if (above) py = y - height - 1;
		popup = new UiBounds(px, py, width, height);
		open = true;
		closing = false;
		openAnim.start(0, 1, UiMotion.POPUP_IN);
		highlighted = firstChecked();
		scrollRows = Math.max(0, highlighted - MAX_ROWS + 1);
	}
	private int visibleRows() {
		return Math.min(MAX_ROWS, items.size());
	}
	private int firstChecked() {
		for (var i = 0; i < items.size(); i++) if (items.get(i).checked()) return i;
		return 0;
	}
	private void adjustScroll() {
		if (highlighted < scrollRows) scrollRows = Math.max(0, highlighted);
		else if (highlighted >= scrollRows + visibleRows()) scrollRows = highlighted - visibleRows() + 1;
	}
	@Override
	public void renderPopup(UiContext ui) {
		if (!open && !closing) return;
		openAnim.update(ui.delta);
		if (closing && openAnim.done()) {
			closing = false;
			return;
		}
		var t = openAnim.value();
		var drawnH = Math.round(popup.height() * (0.92F + 0.08F * t));
		var top = above ? popup.bottom() - drawnH : popup.y();
		if (popup.contains(ui.mouseX, ui.mouseY)) {
			var hovered = indexAt(ui.mouseY);
			if (hovered >= 0) highlighted = hovered;
		}
		highlight.target(highlighted);
		highlight.update(ui.delta);
		ui.painter.pushZ(200);
		ui.painter.pushAlpha(t);
		ui.painter.border(popup.x(), top, popup.width(), drawnH, UiTheme.WINDOW_EDGE);
		ui.painter.rect(popup.x() + 1, top + 1, popup.width() - 2, 1, UiTheme.GROOVE_LIGHT);
		ui.painter.rect(popup.x() + 1, top + 2, popup.width() - 2, Math.max(0, drawnH - 3), UiTheme.SIDEBAR);
		ui.painter.scissorOn(new UiBounds(popup.x() + 1, top + 1, popup.width() - 2, Math.max(0, drawnH - 2)));
		var hy = popup.y() + 1 + (int) Math.round(highlight.get() * ROW_HEIGHT) - scrollRows * ROW_HEIGHT;
		if (highlighted >= 0) ui.painter.rect(popup.x() + 1, hy, popup.width() - 2, ROW_HEIGHT, UiTheme.ROW_HOVER);
		for (var i = scrollRows; i < scrollRows + visibleRows() && i < items.size(); i++) {
			var item = items.get(i);
			var rowY = popup.y() + 1 + i * ROW_HEIGHT;
			var color = !item.enabled() ? UiTheme.DISABLED : i == highlighted ? UiTheme.TEXT : UiTheme.TEXT_2;
			var label = ui.painter.trim(item.label().getString(), popup.width() - 25);
			ui.painter.text(label, popup.x() + 7, rowY + (ROW_HEIGHT - ui.painter.lineHeight()) / 2 + 1, color);
			if (item.checked()) ui.painter.rect(popup.x() + 1, rowY + 2, 2, ROW_HEIGHT - 4, ui.accent().base());
		}
		ui.painter.scissorOff();
		ui.painter.popAlpha();
		ui.painter.popZ();
	}
	@Override
	public boolean isOverPopup(double mouseX, double mouseY) {
		return open && !closing && popup.contains(mouseX, mouseY);
	}
	@Override
	public boolean popupMouseClicked(UiContext ui, double mouseX, double mouseY, int button) {
		if (!isOverPopup(mouseX, mouseY)) return false;
		return button == 0;
	}
	@Override
	public boolean popupMouseReleased(UiContext ui, double mouseX, double mouseY, int button) {
		if (!open || button != 0) return false;
		var index = indexAt(mouseY);
		if (index >= 0 && items.get(index).enabled() && isOverPopup(mouseX, mouseY)) {
			UiSound.click();
			close();
			onSelect.accept(index);
		}
		return true;
	}
	@Override
	public boolean popupMouseScrolled(double amount) {
		if (!open || items.isEmpty()) return false;
		var direction = (int) Math.signum(amount);
		var next = highlighted;
		for (var i = 0; i < items.size(); i++) {
			next = Math.floorMod(next - direction, items.size());
			if (items.get(next).enabled()) break;
		}
		highlighted = next;
		adjustScroll();
		return true;
	}
	@Override
	public boolean popupKeyPressed(UiContext ui, int keyCode, int scanCode, int modifiers) {
		if (!open) return false;
		if (keyCode == 256) {
			close();
			return true;
		}
		if (keyCode == 265 || keyCode == 264) {
			var direction = keyCode == 265 ? -1 : 1;
			for (var i = 0; i < items.size(); i++) {
				highlighted = Math.floorMod(highlighted + direction, items.size());
				if (items.get(highlighted).enabled()) break;
			}
			adjustScroll();
			return true;
		}
		if (keyCode == 257 || keyCode == 335) {
			if (highlighted >= 0 && items.get(highlighted).enabled()) {
				UiSound.click();
				close();
				onSelect.accept(highlighted);
			}
			return true;
		}
		return false;
	}
	@Override
	public boolean popupCharTyped(UiContext ui, char codePoint) {
		return false;
	}
	@Override
	public void close() {
		if (!open) return;
		open = false;
		closing = true;
		openAnim.start(openAnim.value(), 0, UiMotion.POPUP_OUT);
	}
	@Override
	public boolean closing() {
		return closing;
	}
	private int indexAt(double mouseY) {
		var row = (int) Math.floor((mouseY - popup.y() - 1) / ROW_HEIGHT);
		var index = row + scrollRows;
		return row >= 0 && row < visibleRows() && index >= 0 && index < items.size() ? index : -1;
	}
	public record Item(Component label, boolean enabled, boolean checked, Runnable action) {}
}
