package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.platform.services.PlatformService;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.*;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
public class NeoForgePlatformService implements PlatformService {
	@Override
	public Path configDir() {
		return FMLPaths.CONFIGDIR.get();
	}
	@Override
	public boolean isModLoaded(String modId) {
		return LoadingModList.get().getModFileById(modId) != null;
	}
	@Override
	public boolean isClient() {
		return FMLEnvironment.dist.isClient();
	}
	@Override
	public boolean isDevelopmentEnvironment() {
		return !FMLLoader.isProduction();
	}
	@Override
	public @Nullable String modDisplayName(String modId) {
		return ModList.get().getModContainerById(modId).map(container -> container.getModInfo().getDisplayName()).orElse(null);
	}
}
