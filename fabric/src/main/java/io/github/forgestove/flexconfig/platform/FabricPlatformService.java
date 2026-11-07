package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.platform.services.PlatformService;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
public class FabricPlatformService implements PlatformService {
	@Override
	public Path configDir() {
		return FabricLoader.getInstance().getConfigDir();
	}
	@Override
	public boolean isModLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}
	@Override
	public boolean isClient() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
	}
	@Override
	public boolean isDevelopmentEnvironment() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}
	@Override
	public @Nullable String modDisplayName(String modId) {
		return FabricLoader.getInstance().getModContainer(modId).map(container -> container.getMetadata().getName()).orElse(null);
	}
}
