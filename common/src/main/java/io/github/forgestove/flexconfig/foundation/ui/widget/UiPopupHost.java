package io.github.forgestove.flexconfig.foundation.ui.widget;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import org.jetbrains.annotations.Nullable;
public class UiPopupHost {
	private @Nullable UiDropdown openDropdown;
	private @Nullable UiDropdown fadingDropdown;
	private @Nullable UiPopup openMenu;
	private @Nullable UiPopup fadingMenu;
	public @Nullable UiPopup active() {
		return openMenu != null ? openMenu : openDropdown;
	}
	public @Nullable UiDropdown openDropdown() {
		return openDropdown;
	}
	public void open(UiPopup popup) {
		close();
		openMenu = popup;
	}
	public void open(UiDropdown dropdown, UiBounds viewport) {
		if (openDropdown == dropdown) {
			close();
			return;
		}
		close();
		dropdown.setViewport(viewport);
		dropdown.setOpen(true);
		openDropdown = dropdown;
	}
	public void close() {
		if (openDropdown != null) {
			openDropdown.beginClose();
			fadingDropdown = openDropdown;
		}
		if (openMenu != null) {
			openMenu.close();
			fadingMenu = openMenu;
		}
		openDropdown = null;
		openMenu = null;
	}
	public void clear() {
		openDropdown = null;
		fadingDropdown = null;
		openMenu = null;
		fadingMenu = null;
	}
	public void render(UiContext ui) {
		if (openMenu != null && openMenu.closing()) {
			fadingMenu = openMenu;
			openMenu = null;
		}
		var popup = active();
		if (popup != null) popup.renderPopup(ui);
		if (fadingMenu != null) {
			fadingMenu.renderPopup(ui);
			if (!fadingMenu.closing()) fadingMenu = null;
		}
		if (fadingDropdown != null) {
			fadingDropdown.renderPopup(ui);
			if (!fadingDropdown.closing()) fadingDropdown = null;
		}
	}
}
