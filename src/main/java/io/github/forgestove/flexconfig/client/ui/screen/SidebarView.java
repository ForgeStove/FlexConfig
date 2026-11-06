package io.github.forgestove.flexconfig.client.ui.screen;
import io.github.forgestove.flexconfig.client.ui.core.*;
import io.github.forgestove.flexconfig.client.ui.model.ConfigUiModel;
import io.github.forgestove.flexconfig.client.ui.widget.UiScrollPane;

import java.util.*;
import java.util.function.IntConsumer;
public class SidebarView extends UiScrollPane {
	private final ConfigUiModel model;
	private final IntConsumer onSelect;
	private int pressedIndex = -1;
	private final UiAnim indicatorTop = new UiAnim(0, UiMotion.INDICATOR_LEAD);
	private final UiAnim indicatorBottom = new UiAnim(UiTheme.TAB_PITCH, UiMotion.INDICATOR_LEAD);
	private int lastActive = -1;
	private final List<UiAnim> hovers = new ArrayList<>();
	private boolean indicatorInit;
	public SidebarView(ConfigUiModel model, IntConsumer onSelect) {
		this.model = model;
		this.onSelect = onSelect;
		setPitch(UiTheme.TAB_PITCH);
	}
	public UiBounds tabBounds(int index) {
		var width = bounds().width() - (shown() ? UiTheme.SCROLLBAR_WIDTH + 2 : 0);
		return new UiBounds(bounds().x(), bounds().y() + index * UiTheme.TAB_PITCH - (int) Math.round(scroll()), width, UiTheme.TAB_PITCH);
	}
	@Override
	public void render(UiContext ui) {
		setContentHeight(model.tabs().size() * UiTheme.TAB_PITCH);
		super.render(ui);
	}
	@Override
	protected void renderContent(UiContext ui) {
		var tabs = model.tabs();
		while (hovers.size() < tabs.size()) hovers.add(new UiAnim(0, UiMotion.HOVER_IN));
		for (var i = 0; i < tabs.size(); i++) {
			var tab = tabs.get(i);
			var item = tabBounds(i);
			var active = i == model.activeIndex();
			var hover = hovers.get(i);
			var hot = item.contains(ui.mouseX, ui.mouseY);
			hover.retune(hot ? UiMotion.HOVER_IN : UiMotion.HOVER_OUT);
			hover.target(hot ? 1 : 0);
			hover.update(ui.delta);
			if (pressedIndex == i) ui.painter.rect(item, UiTheme.ROW_PRESS);
			if (active) {
				ui.painter.rect(item.x(), item.y(), item.width(), 1, UiTheme.GROOVE_LIGHT);
				ui.painter.rect(item.x(), item.y() + 1, item.width(), item.height() - 2, UiTheme.PANEL);
				ui.painter.rect(item.x(), item.bottom() - 1, item.width(), 1, UiTheme.SEP);
			} else if (hover.get() > 0.01) ui.painter.rect(item, UiTheme.mulAlpha(UiTheme.ROW_HOVER, 0.6F * (float) hover.get()));
			var nudge = active ? 0F : (float) hover.get();
			if (nudge != 0F) ui.painter.pushTranslate(nudge, 0F);
			var glyph = new UiBounds(item.x() + 7, item.y() + (item.height() - 7) / 2, 7, 7);
			drawGlyph(ui.painter, i, glyph, active ? ui.accent().base() : UiTheme.TEXT_3);
			var label = ui.painter.trim(tab.title.getString(), item.width() - 34);
			ui.painter.text(
				label,
				item.x() + 18,
				item.y() + (item.height() - ui.painter.lineHeight()) / 2,
				active || hover.get() > 0.5 ? UiTheme.TEXT : UiTheme.TEXT_2
			);
			var dotY = item.y() + item.height() / 2 - 1;
			if (tab.hasError) ui.painter.rect(item.right() - 9, dotY, 2, 2, UiTheme.DANGER);
			else if (tab.changedCount > 0) ui.painter.rect(item.right() - 9, dotY, 2, 2, ui.accent().base());
			if (nudge != 0F) ui.painter.popTranslate();
		}
		var activeIndex = model.activeIndex();
		if (activeIndex >= 0 && activeIndex < tabs.size()) {
			var activeItem = tabBounds(activeIndex);
			if (!indicatorInit) {
				indicatorTop.jumpTo(activeItem.y());
				indicatorBottom.jumpTo(activeItem.bottom());
				indicatorInit = true;
			} else if (activeIndex != lastActive && lastActive >= 0) {
				var down = activeItem.y() > indicatorTop.target();
				indicatorTop.retune(down ? UiMotion.INDICATOR_LEAD : UiMotion.INDICATOR_TRAIL);
				indicatorBottom.retune(down ? UiMotion.INDICATOR_TRAIL : UiMotion.INDICATOR_LEAD);
			}
			lastActive = activeIndex;
			indicatorTop.target(activeItem.y());
			indicatorBottom.target(activeItem.bottom());
			indicatorTop.update(ui.delta);
			indicatorBottom.update(ui.delta);
			var top = (int) Math.round(indicatorTop.get());
			var bottom = (int) Math.round(indicatorBottom.get());
			ui.painter.rect(bounds().x(), top, 3, Math.max(1, bottom - top), ui.accent().base());
		}
	}
	private static void drawGlyph(UiPainter painter, int index, UiBounds b, int color) {
		switch (index % 8) {
			case 0 -> {
				for (var i = 0; i < 3; i++) {
					var y = b.y() + 1 + i * 3;
					painter.rect(b.x(), y, 7, 1, color);
					painter.rect(b.x() + (i == 0 ? 1 : i == 1 ? 4 : 2), y - 1, 2, 3, color);
				}
			}
			case 1 -> {
				for (var i = 0; i < 3; i++) {
					painter.rect(b.x() + 1 + i, b.y() + 2 + i, 1, 1, color);
					painter.rect(b.x() + 5 - i, b.y() + 2 + i, 1, 1, color);
				}
				painter.rect(b.x() + 3, b.y() + 2, 1, 4, color);
			}
			case 2 -> {
				painter.rect(b.x() + 3, b.y(), 1, 7, color);
				painter.rect(b.x(), b.y() + 3, 7, 1, color);
				painter.rect(b.x() + 1, b.y() + 1, 1, 1, color);
				painter.rect(b.x() + 5, b.y() + 5, 1, 1, color);
			}
			case 3 -> {
				painter.border(b.x(), b.y(), 7, 7, color);
				painter.rect(b.x() + 1, b.y() + 1, 5, 1, color);
				painter.rect(b.x() + 1, b.y() + 5, 5, 1, color);
				painter.rect(b.x() + 3, b.y() + 1, 1, 5, color);
			}
			case 4 -> {
				painter.rect(b.x(), b.y(), 7, 1, color);
				for (var i = 1; i < 6; i++) painter.rect(b.x() + i, b.y() + i, 1, 1, color);
				painter.rect(b.x(), b.y() + 6, 7, 1, color);
			}
			case 5 -> {
				painter.border(b.x(), b.y() + 2, 3, 3, color);
				painter.border(b.x() + 4, b.y() + 2, 3, 3, color);
				painter.rect(b.x() + 3, b.y() + 3, 1, 1, color);
			}
			case 6 -> {
				for (var i = 0; i < 3; i++) painter.rect(b.x(), b.y() + i * 3, 7, 1, color);
				painter.rect(b.x() + 2, b.y() + 1, 1, 1, color);
				painter.rect(b.x() + 4, b.y() + 4, 1, 1, color);
			}
			default -> {
				painter.rect(b.x() + 2, b.y(), 3, 1, color);
				painter.rect(b.x() + 1, b.y() + 1, 1, 2, color);
				painter.rect(b.x() + 5, b.y() + 1, 1, 2, color);
				painter.rect(b.x() + 3, b.y() + 3, 1, 1, color);
				painter.rect(b.x() + 3, b.y() + 5, 1, 2, color);
			}
		}
	}
	@Override
	public boolean mouseClicked(UiContext ui, double mouseX, double mouseY, int button) {
		if (button != 0 || !bounds().contains(mouseX, mouseY)) return false;
		if (super.mouseClicked(ui, mouseX, mouseY, button)) return true;
		var tabs = model.tabs();
		var index = (int) ((mouseY - bounds().y() + scroll()) / UiTheme.TAB_PITCH);
		if (index >= 0 && index < tabs.size()) {
			pressedIndex = index;
			return true;
		}
		return true;
	}
	@Override
	public boolean mouseDragged(UiContext ui, double mouseX, double mouseY, int button, double dragX, double dragY) {
		return super.mouseDragged(ui, mouseX, mouseY, button, dragX, dragY);
	}
	@Override
	public boolean mouseReleased(UiContext ui, double mouseX, double mouseY, int button) {
		super.mouseReleased(ui, mouseX, mouseY, button);
		if (pressedIndex < 0) return false;
		var index = pressedIndex;
		pressedIndex = -1;
		if (button != 0 || !tabBounds(index).contains(mouseX, mouseY)) return true;
		onSelect.accept(index);
		UiSound.click();
		return true;
	}
}
