package io.github.forgestove.flexconfig.foundation.ui.model;
import io.github.forgestove.flexconfig.foundation.ui.factory.*;
import net.minecraft.network.chat.Component;

import java.util.List;
/** 参与同页键位冲突检查的行。 */
public interface KeybindConflictAware {
	EntryKeybind keybind();
	Component displayTitle();
	void applyConflict(KeybindState state, List<Component> usages);
}
