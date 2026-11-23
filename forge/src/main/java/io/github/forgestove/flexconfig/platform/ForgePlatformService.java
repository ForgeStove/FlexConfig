package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.platform.services.PlatformService;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.*;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
public class ForgePlatformService implements PlatformService {
	@Override
	public Path configDir() {
		return FMLPaths.CONFIGDIR.get();
	}
	@Override
	public boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
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
