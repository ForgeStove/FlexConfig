package io.github.forgestove.flexconfig.foundation;
import io.github.forgestove.flexconfig.api.*;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
public class ConfigWatchService {
	public static final int INTERVAL_TICKS = 20;
	private static final Map<FileKey, Long> STAMPS = new HashMap<>();
	public static void markCurrent(FileKey key) {
		var path = handlerPath(key);
		if (path == null || !Files.exists(path)) {
			STAMPS.remove(key);
			return;
		}
		try {
			STAMPS.put(key, Files.getLastModifiedTime(path).toMillis());
		} catch (IOException e) {
			STAMPS.remove(key);
		}
	}
	@Nullable
	private static Path handlerPath(FileKey key) {
		return ConfigStore.getRegisteredFileKeys().contains(key) ? ConfigStore.getHandler(key).getPath() : null;
	}
	public static Set<FileKey> poll(Set<FileKey> excluded, ConfigType... types) {
		if (types.length == 0) return Set.of();
		var allowed = EnumSet.copyOf(List.of(types));
		var changed = new LinkedHashSet<FileKey>();
		for (var key : ConfigStore.getRegisteredFileKeys()) {
			if (excluded.contains(key) || !allowed.contains(ConfigStore.getHandler(key).getType())) continue;
			var path = handlerPath(key);
			if (path == null || !Files.exists(path)) continue;
			long stamp;
			try {
				stamp = Files.getLastModifiedTime(path).toMillis();
			} catch (IOException e) {
				continue;
			}
			var previous = STAMPS.put(key, stamp);
			if (previous != null && previous != stamp) changed.add(key);
		}
		return changed;
	}
}
