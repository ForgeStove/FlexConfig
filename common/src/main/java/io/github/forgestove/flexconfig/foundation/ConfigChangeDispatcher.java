package io.github.forgestove.flexconfig.foundation;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.api.registration.ConfigChangeHandler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
/**
 * 批次窗口由 {@code RootConfigNode} 和 {@code CategoryConfigNode} 的写回遍历用 {@link #begin()}/{@link #end()} 划定，
 * 深度归零才派发，handler 执行时配置对象已经是完整的新值；不经过树遍历的单节点写入（快捷键路径）深度为 0，
 * 调用 {@link #record} 时立即派发。只在客户端保存时使用；单线程，没有做线程安全。
 */
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
	/** 无参构造并缓存；构造失败也缓存 {@code null}，避免每次保存都重试。 */
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
	/** 同一 handler 类仅保留首次记录，派发时按类执行一次。 */
	public static void record(Class<? extends ConfigChangeHandler> type, String path, Object oldValue, Object newValue) {
		PENDING.putIfAbsent(type, new Pending(path, oldValue, newValue));
		if (depth == 0) flush();
	}
	private record Pending(String path, Object oldValue, Object newValue) {}
}
