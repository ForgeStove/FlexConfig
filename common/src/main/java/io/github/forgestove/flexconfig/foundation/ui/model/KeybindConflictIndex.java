package io.github.forgestove.flexconfig.foundation.ui.model;
import io.github.forgestove.flexconfig.foundation.ui.core.UiText;
import io.github.forgestove.flexconfig.foundation.ui.factory.*;
import io.github.forgestove.flexconfig.foundation.ui.row.ConfigRow;

import java.util.*;
/** 同页行间与原版按键的冲突检查。 */
public class KeybindConflictIndex {
	public static void apply(List<ConfigRow> rows) {
		var aware = new ArrayList<KeybindConflictAware>();
		for (var row : rows) if (row instanceof KeybindConflictAware target) aware.add(target);
		if (aware.isEmpty()) return;
		var localCount = new HashMap<EntryKeybind, Integer>();
		var localUsages = new HashMap<EntryKeybind, List<KeybindConflictAware>>();
		for (var target : aware) {
			var keybind = target.keybind();
			if (keybind.isUnbound()) continue;
			var physical = keybind.physicalKey();
			localCount.merge(physical, 1, Integer::sum);
			localUsages.computeIfAbsent(physical, ignored -> new ArrayList<>()).add(target);
		}
		for (var target : aware) {
			var keybind = target.keybind();
			if (keybind.isUnbound()) {
				target.applyConflict(KeybindState.UNBOUND, List.of());
				continue;
			}
			var physical = keybind.physicalKey();
			var registered = Keybinds.support().registeredUsages(physical);
			var conflict = localCount.getOrDefault(physical, 0) > 1 || registered.size() > 1;
			if (!conflict) {
				target.applyConflict(KeybindState.BOUND, List.of());
				continue;
			}
			var usages = new ArrayList<UiText>();
			for (var other : localUsages.getOrDefault(physical, List.of())) if (other != target) usages.add(other.displayTitle());
			usages.addAll(registered);
			target.applyConflict(KeybindState.CONFLICT, usages);
		}
	}
}
