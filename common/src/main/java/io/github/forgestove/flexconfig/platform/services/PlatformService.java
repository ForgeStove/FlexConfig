package io.github.forgestove.flexconfig.platform.services;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
public interface PlatformService {
	Path configDir();
	boolean isModLoaded(String modId);
	boolean isClient();
	boolean isDevelopmentEnvironment();
	@Nullable String modDisplayName(String modId);
}
