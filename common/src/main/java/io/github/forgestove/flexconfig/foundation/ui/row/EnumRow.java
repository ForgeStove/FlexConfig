package io.github.forgestove.flexconfig.foundation.ui.row;
import io.github.forgestove.flexconfig.foundation.ConfigTranslation;
import io.github.forgestove.flexconfig.foundation.client.ClientUtil;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.factory.*;
import io.github.forgestove.flexconfig.foundation.ui.model.*;
import io.github.forgestove.flexconfig.foundation.ui.widget.*;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiButton.Style;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiDropdown.Option;
import io.github.forgestove.flexconfig.foundation.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.*;
public class EnumRow extends ValueRow implements KeybindConflictAware {
	private final TriggerKeybindSupport keybind;
	private final UiButton trigger;
	private final @Nullable UiSegmented segmented;
	private final Object[] constants;
	private KeybindState keyState = KeybindState.BOUND;
	private List<Component> conflictUsages = List.of();
	public EnumRow(RowContext ctx, ValueConfigNode node) {
		super(ctx, node);
		constants = node.getValueType().getEnumConstants();
		keybind = new TriggerKeybindSupport(node, ctx);
		var options = new ArrayList<Option>();
		var labels = new ArrayList<Component>();
		for (var constant : constants) {
			var component = label((Enum<?>) constant);
			options.add(new Option(constant, component));
			labels.add(component);
		}
		trigger = new UiButton(Component.empty(), Style.KEYBIND, keybind::beginCapture);
		if (constants.length <= 4) {
			segmented = new UiSegmented(labels, () -> ((Enum<?>) value()).ordinal(), index -> setValue(constants[index]));
			actionBar.add(segmented, Mth.clamp(constants.length * 50, 64, 150)).add(trigger, 96);
		} else {
			segmented = null;
			var dropdown = new UiDropdown(options, this::value, this::setValue, d -> ctx.shell().openDropdown(d));
			actionBar.add(dropdown, UiTheme.CONTROL_WIDTH).add(trigger, 96);
		}
		keybind.register(() -> {
			var next = nextValue();
			writeValue(next);
			if (ClientUtil.mc.player == null) return;
			var title = node.getTitle().copy().withStyle(ChatFormatting.WHITE);
			var valueText = label(next).copy().withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
			ClientUtil.mc.player.displayClientMessage(title.append(": ").append(valueText), true);
		});
	}
	private Component label(Enum<?> value) {
		return ConfigTranslation.translatable("%s.config.enum.%s.%s".formatted(
			ctx.shell().root.modId(),
			value.getDeclaringClass().getSimpleName(),
			value.name()
		));
	}
	private Enum<?> nextValue() {
		var current = (Enum<?>) value();
		return (Enum<?>) constants[(current.ordinal() + 1) % constants.length];
	}
	@Override
	protected String defaultText() {
		var value = node.getDefaultValue();
		return value instanceof Enum<?> constant ? label(constant).getString() : super.defaultText();
	}
	@Override
	public void refresh() {
		super.refresh();
		if (segmented != null) segmented.show(((Enum<?>) value()).ordinal(), true);
		trigger.setEnabled(!state.readOnly);
		trigger.setLabel(keybind.current().getDisplayName());
		trigger.setActive(keybind.capturing());
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
