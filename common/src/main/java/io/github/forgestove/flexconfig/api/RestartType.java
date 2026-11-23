package io.github.forgestove.flexconfig.api;
import java.util.*;
public enum RestartType {
	WORLD,
	/** {@link ConfigType#SERVER} 型配置不能使用这个值。 */
	GAME(ConfigType.SERVER);
	private final Set<ConfigType> invalidTypes;
	RestartType(ConfigType... invalidTypes) {
		this.invalidTypes = EnumSet.noneOf(ConfigType.class);
		this.invalidTypes.addAll(Arrays.asList(invalidTypes));
	}
	public boolean isValid(ConfigType type) {
		return !invalidTypes.contains(type);
	}
}
