package io.github.forgestove.flexconfig.client.gui.im.model;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.flexconfig.client.ClientUtil;
import io.github.forgestove.flexconfig.client.gui.factory.EntryKeybind;
import io.github.forgestove.flexconfig.client.gui.factory.KeybindState;
import io.github.forgestove.flexconfig.client.gui.im.row.ConfigRow;
import net.minecraft.network.chat.Component;
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
			localCount.merge(keybind, 1, Integer::sum);
			localUsages.computeIfAbsent(keybind, ignored -> new ArrayList<>()).add(target);
		}
		var registeredCount = new HashMap<Key, Integer>();
		var registeredUsages = new HashMap<Key, List<Component>>();
		for (var mapping : ClientUtil.mc.options.keyMappings) {
			var key = ClientUtil.boundKey(mapping);
			if (key.equals(InputConstants.UNKNOWN)) continue;
			registeredCount.merge(key, 1, Integer::sum);
			registeredUsages.computeIfAbsent(key, ignored -> new ArrayList<>()).add(Component.translatable(mapping.getName()));
		}
		for (var target : aware) {
			var keybind = target.keybind();
			if (keybind.isUnbound()) {
				target.applyConflict(KeybindState.UNBOUND, List.of());
				continue;
			}
			var conflict = localCount.getOrDefault(keybind, 0) > 1 || registeredCount.getOrDefault(keybind.key(), 0) > 1;
			if (!conflict) {
				target.applyConflict(KeybindState.BOUND, List.of());
				continue;
			}
			var usages = new ArrayList<Component>();
			for (var other : localUsages.getOrDefault(keybind, List.of())) if (other != target) usages.add(other.displayTitle());
			usages.addAll(registeredUsages.getOrDefault(keybind.key(), List.of()));
			target.applyConflict(KeybindState.CONFLICT, usages);
		}
	}
}
