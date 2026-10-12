package io.github.forgestove.flexconfig.client.gui.entry;
import io.github.forgestove.flexconfig.client.*;
import io.github.forgestove.flexconfig.client.gui.ConfigCategoryTab;
import io.github.forgestove.flexconfig.client.gui.components.OreSwitchButton;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.*;
public final class BooleanValueConfigEntry<C> extends CapturableValueConfigEntry<C, Boolean> {
	private final OreSwitchButton valueSwitch;
	public BooleanValueConfigEntry(ConfigCategoryTab<C, Boolean> tab, ValueConfigNode<C, Boolean> node) {
		super(tab, node);
		valueSwitch = new OreSwitchButton(
			OreSwitchButton.preferredWidth(),
			HEIGHT,
			node.getTitle(),
			Boolean.TRUE.equals(getValue()),
			this::setValue
		);
		children.add(valueSwitch);
		registerKeybindTask();
	}
	@Override
	protected void registerKeybindTask() {
		registerTriggerAction(() -> {
			var newVal = !getValue();
			setValue(newVal);
			var title = node.getTitle().copy().withStyle(ChatFormatting.WHITE);
			var valText = newVal
				? Translation.ON_LABEL.copy().withStyle(ChatFormatting.GREEN)
				: Translation.OFF_LABEL.copy().withStyle(ChatFormatting.RED);
			if (ClientUtil.mc.player == null) return;
			ClientUtil.mc.player.displayClientMessage(title.append(": ").append(valText), true);
		});
	}
	@Override
	protected AbstractWidget getValueWidget() {
		return valueSwitch;
	}
	@Override
	public void refresh() {
		super.refresh();
		valueSwitch.setValue(Boolean.TRUE.equals(getValue()));
	}
}
