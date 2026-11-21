package io.github.forgestove.flexconfig.foundation.ui.row;
import io.github.forgestove.flexconfig.foundation.client.*;
import io.github.forgestove.flexconfig.foundation.tree.ValueConfigNode;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.factory.*;
import io.github.forgestove.flexconfig.foundation.ui.model.*;
import io.github.forgestove.flexconfig.foundation.ui.widget.*;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiButton.Style;

import java.util.List;
public class BooleanRow extends ValueRow implements KeybindConflictAware, TriggerKeybindAware {
	private final TriggerKeybindSupport keybind;
	private final UiButton trigger;
	private KeybindState keyState = KeybindState.BOUND;
	private List<UiText> conflictUsages = List.of();
	public BooleanRow(RowContext ctx, ValueConfigNode node) {
		super(ctx, node);
		keybind = new TriggerKeybindSupport(node, ctx);
		trigger = new UiButton(UiText.empty(), Style.KEYBIND, keybind::beginCapture);
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
	private static UiText label(boolean value) {
		return (value ? Translation.ON_LABEL : Translation.OFF_LABEL).copy().withStyle(value ? UiStyle.GREEN : UiStyle.RED);
	}
	private void feedback(boolean value) {
		if (ClientUtil.mc.player == null) return;
		var title = node.getTitle().withStyle(UiStyle.WHITE);
		ClientUtil.mc.player.displayClientMessage(Translation.toComponent(title.append(": ").append(label(value))), true);
	}
	@Override
	public void refresh() {
		super.refresh();
		trigger.setEnabled(!state.readOnly);
		trigger.setLabel(keybind.current().getDisplayName());
		trigger.setActive(keybind.capturing());
		if (keyState == KeybindState.CONFLICT && !conflictUsages.isEmpty()) trigger.tooltip(conflictTooltip(conflictUsages));
		else trigger.tooltip(null);
		if (keybind.changed() && !state.readOnly) state.changed = true;
	}
	@Override
	public boolean hasTriggerKeybind() {
		return keybind.serialized() != null;
	}
	private static UiText conflictTooltip(List<UiText> usages) {
		var used = UiText.empty();
		for (var i = 0; i < usages.size(); i++) {
			if (i > 0) used = used.append(", ");
			used = used.append(usages.get(i));
		}
		return UiText.translatable("controls.keybinds.duplicateKeybinds", used);
	}
	@Override
	public EntryKeybind keybind() {
		return keybind.current();
	}
	@Override
	public UiText displayTitle() {
		return node.getTitle();
	}
	@Override
	public void applyConflict(KeybindState state, List<UiText> usages) {
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
