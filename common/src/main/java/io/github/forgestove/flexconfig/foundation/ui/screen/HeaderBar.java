package io.github.forgestove.flexconfig.foundation.ui.screen;
import io.github.forgestove.flexconfig.foundation.client.*;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.widget.*;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiButton.Style;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiGlyphButton.Glyph;

import java.util.*;
import java.util.function.Consumer;
public class HeaderBar {
	public record Info(
		String modId, String fileName, boolean restartRequired, boolean filtering, int searchCount, boolean pageMenuVisible
	) {}
	private final UiButton undoButton;
	private final UiGlyphButton closeButton;
	private final UiTextField searchField;
	private final UiChip changedChip;
	private final UiButton pageMenuButton;
	private final UiAnim searchWidth = new UiAnim(90, UiMotion.SEARCH_WIDTH);
	public HeaderBar(Runnable onClose, Runnable onUndo, Runnable onPageMenu, Consumer<String> onSearch, Runnable onToggleChanged) {
		undoButton = new UiButton(UiText.empty(), Style.SECONDARY, onUndo);
		undoButton.setVisible(false);
		closeButton = new UiGlyphButton(Glyph.CLOSE, onClose);
		searchField = new UiTextField("");
		searchField.setLeadingSearch(true);
		searchField.setPlaceholder(Translation.SEARCH_PLACEHOLDER);
		searchField.setResponder(onSearch);
		changedChip = new UiChip(UiText.empty(), onToggleChanged);
		pageMenuButton = new UiButton(UiText.literal("..."), Style.SECONDARY, onPageMenu);
		pageMenuButton.tooltip(Translation.SEARCH_MENU);
	}
	public UiTextField searchField() {
		return searchField;
	}
	public UiBounds pageMenuBounds() {
		return pageMenuButton.bounds();
	}
	public void setUndo(int changed, boolean editable) {
		undoButton.setVisible(changed > 0 && editable);
		undoButton.setLabel(Translation.undoChanges(changed));
	}
	public void setChanged(int count, boolean selected) {
		changedChip.setLabel(Translation.searchChanged(count));
		changedChip.setSelected(selected);
	}
	public void setChangedSelected(boolean selected) {
		changedChip.setSelected(selected);
	}
	public void render(UiContext ui, ScreenLayout layout, Info info) {
		var painter = ui.painter;
		var title = ClientUtil.modName(info.modId()).getString().toUpperCase(Locale.ROOT);
		var titleY = layout.header.y() + (layout.header.height() - 3 - 10 + 1) / 2;
		painter.textEmphasis(
			title,
			layout.header.x() + (layout.header.width() - painter.textWidthEmphasis(title)) / 2,
			titleY,
			UiTheme.TITLE_INK
		);
		var headerText = UiText.literal(info.fileName());
		if (info.restartRequired())
			headerText = headerText.append("  ·  ").append(Translation.RESTART_REQUIRED_TITLE.copy().withStyle(UiStyle.RED));
		painter.text(headerText, layout.pageHeader.x(), layout.pageHeader.y() + 4, UiTheme.TEXT_3);
		if (info.filtering()) {
			var count = Translation.searchResults(info.searchCount());
			painter.text(count, layout.pageHeader.x() + painter.textWidth(headerText) + 8, layout.pageHeader.y() + 4, UiTheme.TEXT_3);
		}
		closeButton.setBounds(new UiBounds(
			layout.header.right() - 19,
			layout.header.y() + Math.round((layout.header.height() - 3 - 16) / 2F),
			16,
			16
		));
		closeButton.render(ui);
		var undoWidth = 110;
		undoButton.setBounds(new UiBounds(closeButton.bounds().x() - 4 - undoWidth, layout.header.y() + 4, undoWidth, 14));
		undoButton.render(ui);
		var y = layout.pageHeader.y() + 3;
		var right = layout.pageHeader.right();
		pageMenuButton.setVisible(info.pageMenuVisible());
		if (pageMenuButton.visible()) {
			pageMenuButton.setBounds(new UiBounds(right - 24, y, 24, UiTheme.CONTROL_HEIGHT));
			pageMenuButton.render(ui);
			right -= 28;
		}
		var changedW = Math.min(90, changedChip.preferredWidth());
		changedChip.setBounds(new UiBounds(right - changedW, y, changedW, UiTheme.CONTROL_HEIGHT));
		changedChip.render(ui);
		right -= changedW + 4;
		searchWidth.target(searchField.isFocused(ui) || !searchField.value().isEmpty() ? 140 : 90);
		searchWidth.update(ui.delta);
		searchField.setBounds(new UiBounds(
			right - (int) Math.round(searchWidth.get()),
			y,
			(int) Math.round(searchWidth.get()),
			UiTheme.CONTROL_HEIGHT
		));
		searchField.render(ui);
	}
	public boolean press(UiPointerRouter pointer, UiContext ui, double mouseX, double mouseY, int button) {
		if (pointer.press(ui, changedChip, mouseX, mouseY, button)) return true;
		if (pointer.press(ui, pageMenuButton, mouseX, mouseY, button)) return true;
		if (pointer.press(ui, undoButton, mouseX, mouseY, button)) return true;
		if (pointer.press(ui, closeButton, mouseX, mouseY, button)) return true;
		return pointer.press(ui, searchField, mouseX, mouseY, button);
	}
	public boolean keyPressed(UiContext ui, int keyCode, int scanCode, int modifiers) {
		if (searchField.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		if (changedChip.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		if (pageMenuButton.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		return undoButton.keyPressed(ui, keyCode, scanCode, modifiers);
	}
	public boolean closeKeyPressed(UiContext ui, int keyCode, int scanCode, int modifiers) {
		return closeButton.keyPressed(ui, keyCode, scanCode, modifiers);
	}
	public List<UiControl> focusables() {
		var controls = new ArrayList<UiControl>();
		controls.add(searchField);
		controls.add(changedChip);
		if (pageMenuButton.focusable()) controls.add(pageMenuButton);
		if (undoButton.visible()) controls.add(undoButton);
		controls.add(closeButton);
		return controls;
	}
}
