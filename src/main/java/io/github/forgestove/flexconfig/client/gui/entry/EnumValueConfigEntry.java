package io.github.forgestove.flexconfig.client.gui.entry;

import io.github.forgestove.flexconfig.ConfigTranslation;
import io.github.forgestove.flexconfig.client.ClientUtil;
import io.github.forgestove.flexconfig.client.gui.ConfigCategoryTab;
import io.github.forgestove.flexconfig.client.gui.api.OverlayDropdown;
import io.github.forgestove.flexconfig.client.gui.components.InlineDropdown;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
public class EnumValueConfigEntry<C> extends CapturableValueConfigEntry<C, Enum<?>> implements OverlayDropdown {
	private final InlineDropdown<Enum<?>> dropdown;
	private final String enumClassName;
	public EnumValueConfigEntry(ConfigCategoryTab<C, Enum<?>> tab, ValueConfigNode<C, Enum<?>> node) {
		super(tab, node);
		enumClassName = node.getValueType().getSimpleName();
		var constants = node.getValueType().getEnumConstants();
		var values = new ArrayList<>(List.of(constants));
		dropdown = new InlineDropdown<>(
			WIDTH,
			HEIGHT,
			values,
			this::getValue,
			this::getDisplayComponent,
			this::selectValue,
			tab::listTop,
			tab::listBottom
		);
		children.add(dropdown.topCell());
		registerKeybindTask();
	}
	private Component getDisplayComponent(Enum<?> value) {
		return ConfigTranslation.translatable("%s.config.enum.%s.%s".formatted(tab.screen.root.modId(), enumClassName, value.name()));
	}
	private Enum<?> nextValue() {
		var values = node.getValueType().getEnumConstants();
		return values[(getValue().ordinal() + 1) % values.length];
	}
	@Override
	protected void registerKeybindTask() {
		registerTriggerAction(() -> {
			setValue(nextValue());
			var title = node.getTitle().copy().withStyle(ChatFormatting.WHITE);
			var valText = getDisplayComponent(getValue()).copy().withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
			if (ClientUtil.mc.player == null) return;
			ClientUtil.mc.player.displayClientMessage(title.append(": ").append(valText), true);
		});
	}
	@Override
	protected AbstractWidget getValueWidget() {
		return dropdown.topCell();
	}
	@Override
	public void refresh() {
		super.refresh();
		dropdown.refresh();
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
	private void selectValue(Enum<?> value) {
		setValue(value);
	}
}
