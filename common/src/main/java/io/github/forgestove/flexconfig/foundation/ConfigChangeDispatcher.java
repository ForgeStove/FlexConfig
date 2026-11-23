package io.github.forgestove.flexconfig.foundation;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.api.registration.ConfigChangeHandler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;

public class ConfigChangeDispatcher {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<Class<? extends ConfigChangeHandler>, ConfigChangeHandler> INSTANCES = new HashMap<>();
	private static final Map<Class<? extends ConfigChangeHandler>, Pending> PENDING = new LinkedHashMap<>();
	private static int depth;
	public static void begin() {
		depth++;
	}
	public static void end() {
		if (--depth > 0) return;
		depth = 0;
		flush();
	}
	private static void flush() {
		if (PENDING.isEmpty()) return;
		var batch = new ArrayList<>(PENDING.entrySet());
		PENDING.clear();
		for (var entry : batch) {
			var handler = instance(entry.getKey());
			if (handler == null) continue;
			var pending = entry.getValue();
			try {
				LOGGER.debug(
					"Config change handler [{}] fired at {} ({} -> {})",
					entry.getKey().getSimpleName(),
					pending.path(),
					pending.oldValue(),
					pending.newValue()
				);
				handler.onChange(pending.oldValue(), pending.newValue());
			} catch (Throwable t) {
				LOGGER.error("Config change handler [{}] failed at {}", entry.getKey().getSimpleName(), pending.path(), t);
			}
		}
	}
	@Nullable
	private static ConfigChangeHandler instance(Class<? extends ConfigChangeHandler> type) {
		if (INSTANCES.containsKey(type)) return INSTANCES.get(type);
		ConfigChangeHandler handler = null;
		try {
			handler = type.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			LOGGER.error("Failed to create config change handler [{}]", type.getName(), e);
		}
		INSTANCES.put(type, handler);
		return handler;
	}
	public static void record(Class<? extends ConfigChangeHandler> type, String path, Object oldValue, Object newValue) {
		PENDING.putIfAbsent(type, new Pending(path, oldValue, newValue));
		if (depth == 0) flush();
	}
	private record Pending(String path, Object oldValue, Object newValue) {}
}
