package io.github.forgestove.flexconfig.api.registration;
import io.github.forgestove.flexconfig.foundation.ConfigStore;
import org.jetbrains.annotations.NotNull;

import java.util.List;
/** 下游只读入口：按模组或配置类查询已注册的配置；注册、索引与生命周期由内部 {@link ConfigStore} 负责。 */
public final class ConfigRegistry {
	private ConfigRegistry() {}
	public static List<ConfigHandler<?, ?>> getHandlers(String modId) {
		return ConfigStore.getHandlers(modId);
	}
	public static <C> @NotNull ConfigHandler<C, ?> getHandler(Class<C> configClass) {
		return ConfigStore.getHandler(configClass);
	}
}
