package io.github.forgestove.flexconfig.client.gui.entry;

import io.github.forgestove.flexconfig.client.gui.ConfigCategoryTab;
import io.github.forgestove.flexconfig.client.gui.api.OverlayDropdown;
import io.github.forgestove.flexconfig.client.gui.components.InlineDropdown;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;
/**
 * 任意值集合的下拉选择，供自定义 {@code EntryTypeRegistry} 提供器使用：
 * 候选项与显示文本由调用方决定，不受字段类型限制。
 */
@SuppressWarnings("unused")
public class SelectionValueConfigEntry<C, T> extends ValueConfigEntry<C, T> implements OverlayDropdown {
	protected final InlineDropdown<T> dropdown;
	public SelectionValueConfigEntry(
		ConfigCategoryTab<C, T> tab,
		ValueConfigNode<C, T> node,
		List<T> candidates,
		Function<T, Component> text
	) {
		super(tab, node);
		dropdown = new InlineDropdown<>(
			WIDTH,
			HEIGHT,
			candidates,
			this::getValue,
			text,
			this::setValue,
			tab::listTop,
			tab::listBottom
		);
		children.add(dropdown.topCell());
	}
	@Override
	public void refresh() {
		super.refresh();
		dropdown.refresh();
	}
	@Override
	public void render(
		@NotNull GuiGraphics gui,
		int index,
		int y,
		int x,
		int width,
		int height,
		int mouseX,
		int mouseY,
		boolean hovering,
		float delta
	) {
		dropdown.topCell().active = !isReadOnly();
		renderGui(gui, y, x, width, mouseX, mouseY, delta, lockButton, undoButton, resetButton, dropdown.topCell());
	}
	@Override
	public void lateRender(GuiGraphics gui, int mouseX, int mouseY, float delta) {
		dropdown.lateRender(gui, mouseX, mouseY, delta);
	}
	@Override
	public int getMorePossibleHeight() {
		return dropdown.extraHeight();
	}
	@Override
	public boolean isDropdownOpen() {
		return dropdown.expanded();
	}
	@Override
	public boolean isMouseOverDropdown(double mouseX, double mouseY) {
		return dropdown.isMouseOverMenu(mouseX, mouseY);
	}
	@Override
	public boolean dropdownMouseClicked(double mouseX, double mouseY, int button) {
		return dropdown.menuClicked(mouseX, mouseY, button);
	}
	@Override
	public boolean dropdownMouseScrolled(double mouseX, double mouseY, double vertical) {
		return dropdown.menuScrolled(mouseX, mouseY, vertical);
	}
}
