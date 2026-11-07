package io.github.forgestove.flexconfig.api.registration;
import io.github.forgestove.flexconfig.foundation.ConfigStore;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public class ConfigRegistry {
	public static List<ConfigHandler<?, ?>> getHandlers(String modId) {
		return ConfigStore.getHandlers(modId);
	}
	public static <C> @NotNull ConfigHandler<C, ?> getHandler(Class<C> configClass) {
		return ConfigStore.getHandler(configClass);
	}
}
