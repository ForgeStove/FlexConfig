package io.github.forgestove.flexconfig.foundation.client;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.api.*;
import io.github.forgestove.flexconfig.foundation.ConfigStore;
import org.slf4j.Logger;

import java.io.StringReader;
import java.util.*;
public class ClientServerConfigCache {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<FileKey, CommentedConfig> SYNCED = new HashMap<>();
	public static void accept(String modId, String fileName, String tomlContent) {
		var key = new FileKey(modId, fileName);
		if (!isServerConfig(key)) return;
		CommentedConfig toml;
		try {
			toml = new TomlParser().parse(new StringReader(tomlContent));
		} catch (Exception e) {
			LOGGER.error("Failed to parse server config '{}'", key, e);
			return;
		}
		SYNCED.put(key, toml);
		ConfigStore.getHandler(key).cast().applySynced(toml);
	}
	private static boolean isServerConfig(FileKey key) {
		return ConfigStore.getRegisteredFileKeys().contains(key) && ConfigStore.getHandler(key).getType() == ConfigType.SERVER;
	}
	public static void clear() {
		SYNCED.keySet().forEach(key -> ConfigStore.getHandler(key).unload());
		SYNCED.clear();
	}
}
