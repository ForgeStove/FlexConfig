package io.github.forgestove.flexconfig.client.gui.api;
/** 展开后占用本行下方区域的条目；列表层据此把菜单区域的点击/滚轮优先交给它。 */
public interface OverlayDropdown {
	boolean isDropdownOpen();
	boolean isMouseOverDropdown(double mouseX, double mouseY);
	boolean dropdownMouseClicked(double mouseX, double mouseY, int button);
	default boolean dropdownMouseScrolled(double mouseX, double mouseY, double vertical) {
		return false;
	}
}
