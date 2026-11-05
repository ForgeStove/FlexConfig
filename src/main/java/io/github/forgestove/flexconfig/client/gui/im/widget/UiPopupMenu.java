package io.github.forgestove.flexconfig.client.gui.im.widget;
import io.github.forgestove.flexconfig.client.gui.im.core.*;
import net.minecraft.network.chat.Component;
import java.util.List;
import java.util.function.IntConsumer;
/** 通用弹层列表：行右键菜单、页面菜单、tab 菜单共用。 */
public class UiPopupMenu implements UiPopup {
	private static final int ROW_HEIGHT = 16, PAD = 4, MIN_WIDTH = 110;
	private final List<Item> items;
	private final IntConsumer onSelect;
	private final UiTween openAnim = new UiTween(0);
	private final UiAnim highlight = new UiAnim(0, UiMotion.CONTROL);
	private UiBounds popup = UiBounds.EMPTY;
	private int highlighted = -1;
	private boolean open;
	public UiPopupMenu(List<Item> items, IntConsumer onSelect) {
		this.items = items;
		this.onSelect = onSelect;
	}
	public void place(UiBounds viewport, int x, int y) {
		var width = MIN_WIDTH;
		for (var item : items) width = Math.max(width, 24 + 6 * item.label().getString().length());
		var height = items.size() * ROW_HEIGHT + PAD * 2;
		var below = viewport.bottom() - y;
		var py = below >= height ? y : Math.max(viewport.y(), y - height);
		popup = new UiBounds(Math.min(x, viewport.right() - width), py, width, height);
		open = true;
		openAnim.start(0, 1, UiMotion.POPUP_IN);
		highlighted = firstEnabled();
	}
	private int firstEnabled() {
		for (var i = 0; i < items.size(); i++) if (items.get(i).enabled()) return i;
		return -1;
	}
	@Override
	public void renderPopup(UiContext ui) {
		if (!open) return;
		openAnim.update(ui.delta);
		var t = openAnim.value();
		var drawn = new UiBounds(popup.x(), popup.y() + popup.height() - Math.round(popup.height() * (0.92F + 0.08F * t)), popup.width(), Math.round(popup.height() * (0.92F + 0.08F * t)));
		highlight.target(highlighted);
		highlight.update(ui.delta);
		ui.painter.pushZ(200);
		ui.painter.pushAlpha(t);
		ui.painter.border(drawn, UiTheme.WINDOW_EDGE);
		ui.painter.rect(drawn.x() + 1, drawn.y() + 1, drawn.width() - 2, 1, UiTheme.GROOVE_LIGHT);
		ui.painter.rect(drawn.x() + 1, drawn.y() + 2, drawn.width() - 2, drawn.height() - 3, UiTheme.SIDEBAR);
		var hy = drawn.y() + PAD + (int) Math.round(highlight.get()) * ROW_HEIGHT;
		if (highlighted >= 0) ui.painter.rect(drawn.x() + 1, hy, drawn.width() - 2, ROW_HEIGHT, UiTheme.ROW_HOVER);
		for (var i = 0; i < items.size(); i++) {
			var item = items.get(i);
			var rowY = drawn.y() + PAD + i * ROW_HEIGHT;
			var color = !item.enabled() ? UiTheme.DISABLED : i == highlighted ? UiTheme.TEXT : UiTheme.TEXT_2;
			var label = ui.painter.trim(item.label().getString(), drawn.width() - 20);
			ui.painter.text(label, drawn.x() + 8, rowY + (ROW_HEIGHT - ui.painter.lineHeight()) / 2, color);
			if (item.checked()) ui.painter.rect(drawn.x() + 1, rowY + 2, 2, ROW_HEIGHT - 4, ui.accent().base());
		}
		ui.painter.popAlpha();
		ui.painter.popZ();
	}
	@Override
	public boolean isOverPopup(double mouseX, double mouseY) {
		return open && popup.contains(mouseX, mouseY);
	}
	@Override
	public boolean popupMouseClicked(UiContext ui, double mouseX, double mouseY, int button) {
		if (!isOverPopup(mouseX, mouseY)) return false;
		if (button != 0) return true;
		highlighted = indexAt(mouseY);
		highlight.snap(Math.max(0, highlighted));
		return true;
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
		open = false;
	}
	private int indexAt(double mouseY) {
		var index = (int) ((mouseY - popup.y() - PAD) / ROW_HEIGHT);
		return index >= 0 && index < items.size() ? index : -1;
	}
	public record Item(Component label, boolean enabled, boolean checked, Runnable action) {}
}
