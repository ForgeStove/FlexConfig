package io.github.forgestove.flexconfig.foundation;
import io.github.forgestove.flexconfig.api.ConfigType;
import io.github.forgestove.flexconfig.platform.FlexConfigServices;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
public class ConfigNaming {
	private static final String SERVER_CONFIG_DIR = "serverconfig";
	public static String fileName(String modId, ConfigType type, @Nullable String explicit) {
		if (explicit != null && !explicit.isBlank()) return explicit.endsWith(".toml") ? explicit : explicit + ".toml";
		return "%s-%s.toml".formatted(modId, type.extension());
	}
	public static Path resolve(ConfigType type, String fileName, @Nullable MinecraftServer server) {
		if (type == ConfigType.SERVER && server != null)
			return server.getWorldPath(LevelResource.ROOT).resolve(SERVER_CONFIG_DIR + "/" + fileName);
		return configDirPath(fileName);
	}
	public static Path configDirPath(String fileName) {
		return FlexConfigServices.PLATFORM.configDir().resolve(fileName);
	}
}
