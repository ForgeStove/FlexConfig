package io.github.forgestove.flexconfig.client.gui.entry;
import io.github.forgestove.flexconfig.client.*;
import io.github.forgestove.flexconfig.client.gui.ConfigCategoryTab;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
public final class BooleanValueConfigEntry<C> extends CapturableValueConfigEntry<C, Boolean> {
	private final Button valueSwitch;
	public BooleanValueConfigEntry(ConfigCategoryTab<C, Boolean> tab, ValueConfigNode<C, Boolean> node) {
		super(tab, node);
		valueSwitch = Button.builder(label(getValue()), b -> setValue(!getValue())).size(HALF_VALUE_WIDTH, HEIGHT).build();
		children.add(valueSwitch);
		registerKeybindTask();
	}
	private static Component label(boolean value) {
		return value
			? Translation.ON_LABEL.copy().withStyle(ChatFormatting.GREEN)
			: Translation.OFF_LABEL.copy().withStyle(ChatFormatting.RED);
	}
	@Override
	protected void registerKeybindTask() {
		registerTriggerAction(() -> {
			var newVal = !getValue();
			setValue(newVal);
			var title = node.getTitle().copy().withStyle(ChatFormatting.WHITE);
			if (ClientUtil.mc.player == null) return;
			ClientUtil.mc.player.displayClientMessage(title.append(": ").append(label(newVal)), true);
		});
	}
	@Override
	protected AbstractWidget getValueWidget() {
		return valueSwitch;
	}
	@Override
	public void refresh() {
		super.refresh();
		valueSwitch.setMessage(label(getValue()));
	}
}
