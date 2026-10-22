package io.github.forgestove.flexconfig.client.gui.entry;
import io.github.forgestove.flexconfig.client.*;
import io.github.forgestove.flexconfig.client.gui.ConfigCategoryTab;
import io.github.forgestove.flexconfig.client.gui.FlexGuiTextures;
import io.github.forgestove.flexconfig.client.gui.components.FlexIconButton;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
public final class BooleanValueConfigEntry<C> extends CapturableValueConfigEntry<C, Boolean> {
	private static final int ON_COLOR = 0x55FF55, OFF_COLOR = 0xFF5555;
	private final FlexIconButton valueSwitch;
	public BooleanValueConfigEntry(ConfigCategoryTab<C, Boolean> tab, ValueConfigNode<C, Boolean> node) {
		super(tab, node);
		valueSwitch = new FlexIconButton(HALF_VALUE_WIDTH, HEIGHT, icon(getValue()), b -> setValue(!getValue()));
		valueSwitch.setIconColor(color(getValue()));
		children.add(valueSwitch);
		registerKeybindTask();
	}
	private static FlexGuiTextures icon(boolean value) {
		return value ? FlexGuiTextures.ICON_ON : FlexGuiTextures.ICON_OFF;
	}
	private static int color(boolean value) {
		return value ? ON_COLOR : OFF_COLOR;
	}
	/** 聊天反馈用的文字标签，与图标同色。 */
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
		valueSwitch.setIcon(icon(getValue()));
		valueSwitch.setIconColor(color(getValue()));
	}
}
