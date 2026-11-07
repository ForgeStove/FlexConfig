package io.github.forgestove.flexconfig.foundation.server;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.api.FileKey;
import io.github.forgestove.flexconfig.foundation.ConfigStore;
import io.github.forgestove.flexconfig.foundation.network.ConfigNetwork;
import org.slf4j.Logger;
/** 把锁定的配置项应用到服务端自身的活动配置；先重载撤销旧锁值，再逐条应用，解锁后能恢复原值。 */
public class ServerLockApplier {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static void applyAll(FileKey key, ServerConfigLockStore store) {
		if (!ConfigStore.getRegisteredFileKeys().contains(key)) return;
		ConfigStore.resetToSaved(key);
		apply(key, store);
	}
	private static void apply(FileKey key, ServerConfigLockStore store) {
		if (!ConfigStore.isLockable(key)) return;
		var paths = store.lockedPaths();
		if (paths.isEmpty()) return;
		var handler = ConfigStore.getHandler(key).cast();
		Object locked;
		try {
			locked = handler.getSerializer().deserializeFrom(store.toConfig());
		} catch (Exception e) {
			LOGGER.error("Failed to apply config locks for '{}'", key, e);
			return;
		}
		var root = handler.getConfigTree();
		for (var path : paths) {
			var node = root.getValueNode(path);
			if (node == null) continue;
			var value = node.getActiveValue(locked);
			if (value == null) continue;
			ConfigStore.applyLockedValue(key, node, value);
		}
	}
	/** 配置文件被外部重载后，重新叠加锁定值；调用方已经重新读取文件，这里不再读取。 */
	public static void reapply(FileKey key) {
		var store = ConfigNetwork.getLockStore(key);
		if (store == null) return;
		apply(key, store);
	}
}
