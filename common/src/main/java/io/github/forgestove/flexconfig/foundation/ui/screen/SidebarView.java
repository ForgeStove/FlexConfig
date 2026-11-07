package io.github.forgestove.flexconfig.foundation.ui.screen;
import io.github.forgestove.flexconfig.foundation.ui.FlexGuiTextures;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.model.ConfigUiModel;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiScrollPane;

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
			if (active) {
				ui.painter.rect(item.x(), item.y(), item.width(), 1, UiTheme.GROOVE_LIGHT);
				ui.painter.rect(item.x(), item.y() + 1, item.width(), item.height() - 2, UiTheme.PANEL);
				ui.painter.rect(item.x(), item.bottom() - 1, item.width(), 1, UiTheme.SEP_DARK);
			} else if (hover.get() > 0.01) ui.painter.rect(item, UiTheme.mulAlpha(UiTheme.ROW_HOVER, 0.6F * (float) hover.get()));
			// 按压底色放在分类背景之后，激活行也有同样的变暗反馈
			if (pressedIndex == i) ui.painter.rect(item, UiTheme.ROW_PRESS);
			var nudge = active ? 0F : (float) hover.get();
			if (nudge != 0F) ui.painter.pushTranslate(nudge, 0F);
			var icon = FlexGuiTextures.of(tab.node.getIcon());
			if (icon != null) ui.painter.texture(icon.texture, new UiBounds(item.x() + 6, item.y() + (item.height() - 16) / 2, 16, 16));
			var textInset = icon == null ? 10 : 26;
			var label = ui.painter.trim(tab.title.getString(), item.width() - textInset - 16);
			ui.painter.text(
				label,
				item.x() + textInset,
				item.y() + (item.height() - ui.painter.lineHeight()) / 2,
				active || hover.get() > 0.5 ? UiTheme.TEXT : UiTheme.TEXT_2
			);
			var dotY = item.y() + item.height() / 2 - 1;
			if (tab.hasError) ui.painter.rect(item.right() - 9, dotY, 2, 2, UiTheme.DANGER);
			else if (tab.changedCount > 0) ui.painter.rect(item.right() - 9, dotY, 2, 2, ui.accent().bright());
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
			ui.painter.rect(bounds().x(), top + 1, 3, Math.max(0, bottom - top - 2), ui.accent().base());
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
