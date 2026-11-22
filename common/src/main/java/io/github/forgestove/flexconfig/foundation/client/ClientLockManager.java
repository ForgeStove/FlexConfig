package io.github.forgestove.flexconfig.foundation.client;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.api.FileKey;
import io.github.forgestove.flexconfig.foundation.ConfigStore;
import io.github.forgestove.flexconfig.foundation.LockValueCodec;
import io.github.forgestove.flexconfig.foundation.network.ConfigLockPayload;
import io.github.forgestove.flexconfig.platform.FlexConfigServices;
import org.jetbrains.annotations.*;
import org.slf4j.Logger;

import java.io.StringReader;
import java.util.*;
public class ClientLockManager {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<LockKey, Boolean> pendingLocks = new LinkedHashMap<>();
	private static final Map<FileKey, Map<String, String>> lockedConfigs = new HashMap<>();
	private static final Map<LockKey, Object> originalValues = new HashMap<>();
	private static final Map<FileKey, CommentedConfig> syncedConfigs = new HashMap<>();
	private static boolean receivedSync = false;
	public static void setLocks(String modId, String fileName, String tomlContent) {
		if (ClientUtil.mc.getConnection() == null && !ClientUtil.mc.isSingleplayer()) return;
		var key = new FileKey(modId, fileName);
		if (!ConfigStore.isLockable(key)) return;
		receivedSync = true;
		CommentedConfig tomlConfig;
		try {
			tomlConfig = new TomlParser().parse(new StringReader(tomlContent));
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return;
		}
		var newLocks = new HashMap<String, String>();
		collectPaths(tomlConfig, "", newLocks);
		var handler = ConfigStore.getHandler(key).cast();
		var root = handler.getConfigTree();
		var config = handler.getConfig();
		var oldLocks = lockedConfigs.getOrDefault(key, Map.of());
		for (var oldEntry : oldLocks.entrySet()) {
			if (newLocks.containsKey(oldEntry.getKey())) continue;
			var original = originalValues.remove(new LockKey(key, oldEntry.getKey()));
			if (original == null) continue;
			var node = root.getValueNode(oldEntry.getKey());
			if (node == null) continue;
			ConfigStore.applyLockedValue(key, node, original);
			node.resetToActive(config);
		}
		for (var path : newLocks.keySet()) {
			var node = root.getValueNode(path);
			if (node == null) continue;
			originalValues.putIfAbsent(new LockKey(key, path), node.getActiveValue(config));
		}
		lockedConfigs.put(key, newLocks);
		syncedConfigs.put(key, tomlConfig);
		reapply(key);
	}
	public static void reapply(FileKey key) {
		var lockedPaths = lockedConfigs.get(key);
		var tomlConfig = syncedConfigs.get(key);
		if (lockedPaths == null || lockedPaths.isEmpty() || tomlConfig == null || !ConfigStore.isLockable(key)) return;
		var handler = ConfigStore.getHandler(key).cast();
		var root = handler.getConfigTree();
		Object instance;
		try {
			instance = handler.getSerializer().deserializeFrom(tomlConfig);
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
			return;
		}
		var config = handler.getConfig();
		for (var path : lockedPaths.keySet()) {
			var node = root.getValueNode(path);
			if (node == null) continue;
			var typedValue = node.getActiveValue(instance);
			if (typedValue == null) continue;
			ConfigStore.applyLockedValue(key, node, typedValue);
			node.resetToActive(config);
		}
	}
	private static void collectPaths(CommentedConfig config, String prefix, Map<String, String> result) {
		config.entrySet().forEach(entry -> {
			var key = entry.getKey();
			var value = entry.getValue();
			var path = prefix.isEmpty() ? key : prefix + "." + key;
			if (value instanceof CommentedConfig sub) collectPaths(sub, path, result);
			else result.put(path, String.valueOf(value));
		});
	}
	@Nullable
	public static Boolean getPendingLock(FileKey file, String configId) {
		return pendingLocks.get(new LockKey(file, configId));
	}
	public static void setPendingLock(FileKey file, String configId, boolean shouldLock) {
		var key = new LockKey(file, configId);
		if (!pendingLocks.containsKey(key)) pendingLocks.put(key, shouldLock);
		else pendingLocks.remove(key);
	}
	public static void clearPendingLock(FileKey file, String configId) {
		pendingLocks.remove(new LockKey(file, configId));
	}
	public static boolean hasPendingLocks() {
		return !pendingLocks.isEmpty();
	}
	public static void clearPendingLocks() {
		pendingLocks.clear();
	}
	public static void flushPendingLocks() {
		if (pendingLocks.isEmpty()) return;
		if (ClientUtil.mc.player == null) {
			pendingLocks.clear();
			return;
		}
		for (var entry : new ArrayList<>(pendingLocks.entrySet())) {
			var key = entry.getKey();
			if (!ConfigStore.isLockable(key.file())) continue;
			String value;
			if (entry.getValue()) value = getCurrentValueAsString(key.file(), key.path());
			else {
				value = "";
				updateOriginalValue(key.file(), key.path());
			}
			FlexConfigServices.NETWORK.sendToServer(new ConfigLockPayload(key.file().modId(), key.file().fileName(), key.path(), value));
		}
		pendingLocks.clear();
	}
	private static @NotNull String getCurrentValueAsString(FileKey file, String configId) {
		var node = ConfigStore.getRootConfigNode(file).getValueNode(configId);
		var config = ConfigStore.getActiveConfig(file);
		if (node == null || config == null) return "";
		var value = node.getActiveValue(config);
		return value == null ? "" : LockValueCodec.format(node, value);
	}
	private static void updateOriginalValue(FileKey file, String configId) {
		var node = ConfigStore.getRootConfigNode(file).getValueNode(configId);
		var config = ConfigStore.getActiveConfig(file);
		if (node == null || config == null) return;
		originalValues.put(new LockKey(file, configId), node.getActiveValue(config));
	}
	public static boolean hasReceivedSync() {
		return receivedSync;
	}
	public static void clear() {
		ConfigStore.getRegisteredFileKeys().forEach(ConfigStore::resetToSaved);
		originalValues.clear();
		lockedConfigs.clear();
		syncedConfigs.clear();
		pendingLocks.clear();
		receivedSync = false;
	}
	public static boolean isLocked(FileKey file, String configId) {
		var fileLocks = lockedConfigs.get(file);
		return fileLocks != null && fileLocks.containsKey(configId);
	}
	private record LockKey(FileKey file, String path) {}
}
