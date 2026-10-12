package io.github.forgestove.flexconfig.api;
import java.util.*;
/** 配置更改完全生效所需的动作。 */
public enum RestartType {
	/** 退出到标题界面再进世界即可，不用重启进程。 */
	WORLD,
	/** 重启整个游戏进程（不能用于 {@link ConfigType#SERVER} 型配置）。 */
	GAME(ConfigType.SERVER);
	private final Set<ConfigType> invalidTypes;
	RestartType(ConfigType... invalidTypes) {
		this.invalidTypes = EnumSet.noneOf(ConfigType.class);
		this.invalidTypes.addAll(Arrays.asList(invalidTypes));
	}
	/** 配置树构建期用它校验。 */
	public boolean isValid(ConfigType type) {
		return !invalidTypes.contains(type);
	}
}
