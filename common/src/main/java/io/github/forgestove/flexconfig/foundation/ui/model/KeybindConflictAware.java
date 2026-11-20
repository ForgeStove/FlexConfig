package io.github.forgestove.flexconfig.foundation.ui.model;
import io.github.forgestove.flexconfig.foundation.ui.core.UiText;
import io.github.forgestove.flexconfig.foundation.ui.factory.*;

import java.util.List;
/** 参与同页键位冲突检查的行。 */
public interface KeybindConflictAware {
	EntryKeybind keybind();
	UiText displayTitle();
	void applyConflict(KeybindState state, List<UiText> usages);
}
