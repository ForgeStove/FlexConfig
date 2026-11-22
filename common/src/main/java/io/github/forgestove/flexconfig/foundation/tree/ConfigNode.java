package io.github.forgestove.flexconfig.foundation.tree;
import io.github.forgestove.flexconfig.api.RestartType;
import io.github.forgestove.flexconfig.foundation.ui.core.UiText;
import org.jetbrains.annotations.Nullable;
public interface ConfigNode {
	@Nullable UiText getTooltip();
	void resetToDefault();
	void resetToActive(Object config);
	@Nullable RestartType restartType(Object config);
	boolean isDefaultValue(Object config);
	boolean isActiveValue(Object config);
	@Nullable UiText validate(Object config);
	void copy(Object from, Object to);
	void copyForRuntime(Object from, Object to);
	void flushPending(Object to);
	void applyRestart(Object saved, Object active, RestartType restartType);
	void writeEditingToConfig(Object config);
	default String getPath() {
		return "";
	}
}
