package io.github.forgestove.flexconfig.api;
import java.util.*;
/** 按严重程度递增声明：节点聚合重启级别时取最重的一档。 */
public enum RestartType {
	WORLD,
	/** 不能用于 {@link ConfigType#SERVER} 型配置。 */
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
