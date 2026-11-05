package io.github.forgestove.flexconfig.client.gui.im.row;
import io.github.forgestove.flexconfig.client.ClientUtil;
import io.github.forgestove.flexconfig.client.Translation;
import io.github.forgestove.flexconfig.client.gui.factory.EntryKeybind;
import io.github.forgestove.flexconfig.client.gui.factory.KeybindState;
import io.github.forgestove.flexconfig.client.gui.im.model.*;
import io.github.forgestove.flexconfig.client.gui.im.widget.*;
import io.github.forgestove.flexconfig.client.gui.im.widget.UiButton.Style;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import java.util.List;
public class BooleanRow extends ValueRow implements KeybindConflictAware {
	private final TriggerKeybindSupport keybind;
	private final UiButton trigger;
	private KeybindState keyState = KeybindState.BOUND;
	private List<Component> conflictUsages = List.of();
	public BooleanRow(RowContext ctx, ValueConfigNode<?, ?> node) {
		super(ctx, node);
		keybind = new TriggerKeybindSupport(node, ctx);
		trigger = new UiButton(Component.empty(), Style.SECONDARY, keybind::beginCapture);
		var toggle = new UiToggle(this::booleanValue, this::setValue);
		actionBar.add(toggle, 34).add(trigger, 96);
		keybind.register(() -> {
			var next = !booleanValue();
			writeValue(next);
			feedback(next);
		});
	}
	private boolean booleanValue() {
		return Boolean.TRUE.equals(value());
	}
	private static Component label(boolean value) {
		return value
			? Translation.ON_LABEL.copy().withStyle(ChatFormatting.GREEN)
			: Translation.OFF_LABEL.copy().withStyle(ChatFormatting.RED);
	}
	private void feedback(boolean value) {
		if (ClientUtil.mc.player == null) return;
		var title = node.getTitle().copy().withStyle(ChatFormatting.WHITE);
		ClientUtil.mc.player.displayClientMessage(title.append(": ").append(label(value)), true);
	}
	@Override
	public void refresh() {
		super.refresh();
		trigger.setEnabled(!state.readOnly);
		var keyText = keybind.current().getDisplayName().copy().withStyle(keyState.color());
		trigger.setLabel(keybind.capturing() ? Component.literal("> ").append(keyText).append(" <") : keyText);
		if (keyState == KeybindState.CONFLICT && !conflictUsages.isEmpty()) trigger.tooltip(conflictTooltip(conflictUsages));
		else trigger.tooltip(null);
		if (keybind.changed() && !state.readOnly) state.changed = true;
		if (keybind.serialized() != null && !state.readOnly) reset.setEnabled(true);
	}
	private static Component conflictTooltip(List<Component> usages) {
		var used = Component.empty();
		for (var i = 0; i < usages.size(); i++) {
			if (i > 0) used = used.append(Component.literal(", "));
			used = used.append(usages.get(i));
		}
		return Component.translatable("controls.keybinds.duplicateKeybinds", used);
	}
	@Override
	public EntryKeybind keybind() {
		return keybind.current();
	}
	@Override
	public Component displayTitle() {
		return node.getTitle();
	}
	@Override
	public void applyConflict(KeybindState state, List<Component> usages) {
		keyState = state;
		conflictUsages = usages;
	}
	@Override
	public void resetToDefault() {
		keybind.resetToDefault();
		super.resetToDefault();
	}
	@Override
	public void resetToActive() {
		keybind.resetToActive();
		super.resetToActive();
	}
}
