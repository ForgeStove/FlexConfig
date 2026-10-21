package io.github.forgestove.flexconfig.client.gui.entry;
import io.github.forgestove.flexconfig.ConfigTranslation;
import io.github.forgestove.flexconfig.client.ClientUtil;
import io.github.forgestove.flexconfig.client.gui.ConfigCategoryTab;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
@SuppressWarnings("rawtypes")
public final class EnumValueConfigEntry<C> extends CapturableValueConfigEntry<C, Enum> {
	private final Button dropdownButton;
	private final String enumClassName;
	public EnumValueConfigEntry(ConfigCategoryTab<C, Enum> tab, ValueConfigNode<C, Enum> node) {
		super(tab, node);
		enumClassName = node.getValueType().getSimpleName();
		dropdownButton = Button.builder(getDisplayComponent(getValue()), b -> selectValue(nextValue())).size(HALF_VALUE_WIDTH, HEIGHT).build();
		children.add(dropdownButton);
		registerKeybindTask();
	}
	private Component getDisplayComponent(Enum<?> value) {
		return ConfigTranslation.translatable("%s.config.enum.%s.%s".formatted(tab.screen.root.modId(), enumClassName, value.name()));
	}
	private Enum<?> nextValue() {
		var values = node.getValueType().getEnumConstants();
		return (Enum<?>) values[(getValue().ordinal() + 1) % values.length];
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
		return dropdownButton;
	}
	@Override
	public void refresh() {
		super.refresh();
		dropdownButton.setMessage(getDisplayComponent(getValue()));
	}
	private void selectValue(Enum<?> value) {
		setValue(value);
		dropdownButton.setMessage(getDisplayComponent(value));
	}
}
