package io.github.forgestove.flexconfig;
import io.github.forgestove.flexconfig.api.ConfigType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
/**
 * 配置文件的外部改动探测（修改时间轮询）。调用方负责节流与分工：客户端轮询非 SERVER 型，服务端轮询 SERVER 型。
 */
public final class ConfigWatchService {
	/** 轮询间隔（tick）。 */
	public static final int INTERVAL_TICKS = 20;
	/** 上次见到的修改时间；为 {@code null} 表示该文件尚未建立基线。 */
	private static final Map<FileKey, Long> STAMPS = new HashMap<>();
	/** 记录当前修改时间为基线。读写完成后调用，避免把自己的改动当成外部改动。 */
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
		return ConfigRegistry.getRegisteredFileKeys().contains(key) ? ConfigRegistry.getHandler(key).getPath() : null;
	}
	/**
	 * 返回自上次轮询以来被外部改动过的文件，并更新基线；首次见到只建基线不报告。
	 * {@code excluded}（如界面正在编辑的文件）会完全跳过且不更新基线，退出排除后仍能被检出。
	 */
	public static Set<FileKey> poll(Set<FileKey> excluded, ConfigType... types) {
		if (types.length == 0) return Set.of();
		var allowed = EnumSet.copyOf(List.of(types));
		var changed = new LinkedHashSet<FileKey>();
		for (var key : ConfigRegistry.getRegisteredFileKeys()) {
			if (excluded.contains(key) || !allowed.contains(ConfigRegistry.getHandler(key).getType())) continue;
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
