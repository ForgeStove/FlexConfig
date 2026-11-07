package io.github.forgestove.flexconfig.foundation.network;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.api.*;
import io.github.forgestove.flexconfig.foundation.ConfigStore;
import io.github.forgestove.flexconfig.foundation.*;
import io.github.forgestove.flexconfig.foundation.client.*;
import io.github.forgestove.flexconfig.foundation.server.*;
import io.github.forgestove.flexconfig.foundation.ui.ConfigScreen;
import io.github.forgestove.flexconfig.platform.FlexConfigServices;
import io.github.forgestove.flexconfig.platform.services.*;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
public class ConfigNetwork {
	private static final Logger LOGGER = LogUtils.getLogger();
	/** 每个 (模组, 配置文件) 一份锁定存储。 */
	private static final Map<FileKey, ServerConfigLockStore> lockStores = new HashMap<>();
	public static void register(PayloadRegistrar payloads) {
		payloads.clientToServer(
			ConfigLockPayload.CHANNEL,
			ConfigLockPayload.class,
			ConfigLockPayload::decode,
			ConfigNetwork::handleConfigLockServer
		);
		payloads.serverToClient(
			ConfigSyncPayload.CHANNEL,
			ConfigSyncPayload.class,
			ConfigSyncPayload::decode,
			ConfigNetwork::handleConfigSyncClient
		);
		payloads.serverToClient(
			ServerConfigPayload.CHANNEL,
			ServerConfigPayload.class,
			ServerConfigPayload::decode,
			ConfigNetwork::handleServerConfigClient
		);
	}
	// -- 数据包处理器 --
	private static void handleConfigLockServer(ConfigLockPayload payload, PayloadContext context) {
		var player = context.player();
		if (player == null) return;
		if (!player.hasPermissions(2)) return;
		var key = new FileKey(payload.modId(), payload.fileName());
		var lockStore = lockStores.get(key);
		if (lockStore == null) return;
		if (payload.value().isEmpty()) lockStore.unlockEntry(payload.configId());
		else {
			// 字符串 → TOML 原生值：按路径查到节点，交给 LockValueCodec 按字段类型转（找不到节点或类型不支持就忽略这条）
			var node = ConfigStore.getRootConfigNode(key).getValueNode(payload.configId());
			var typed = node == null ? null : LockValueCodec.toToml(node, payload.value());
			if (typed == null) return;
			lockStore.lockEntry(payload.configId(), typed);
		}
		ServerLockApplier.applyAll(key, lockStore);
		broadcastLocks();
	}
	private static void handleConfigSyncClient(ConfigSyncPayload payload, PayloadContext context) {
		ClientLockManager.setLocks(payload.modId(), payload.fileName(), payload.tomlContent());
		context.enqueue(() -> {if (ClientUtil.mc.screen instanceof ConfigScreen<?, ?> configScreen) configScreen.refresh();});
	}
	private static void handleServerConfigClient(ServerConfigPayload payload, PayloadContext context) {
		ClientServerConfigCache.accept(payload.modId(), payload.fileName(), payload.tomlContent());
	}
	private static void broadcastLocks() {
		if (lockStores.isEmpty()) return;
		lockStores.forEach((key, value) -> {
			var payload = new ConfigSyncPayload(key.modId(), key.fileName(), value.toTomlString());
			FlexConfigServices.NETWORK.sendToAllPlayers(payload);
		});
	}
	public static void setLockStore(FileKey key, ServerConfigLockStore store) {
		lockStores.put(key, store);
	}
	/** 取某份配置的锁定存储，未注册时返回 {@code null}。 */
	public static @Nullable ServerConfigLockStore getLockStore(FileKey key) {
		return lockStores.get(key);
	}
	public static void saveAll() {
		lockStores.values().forEach(ServerConfigLockStore::save);
	}
	public static void clearAll() {
		lockStores.clear();
	}
	public static void sendLocksToPlayer(ServerPlayer player) {
		lockStores.forEach((key, value) -> FlexConfigServices.NETWORK.sendToPlayer(
			player,
			new ConfigSyncPayload(key.modId(), key.fileName(), value.toTomlString())
		));
	}
	/** 下发的是文件值而非活动值，锁值由 {@link ConfigSyncPayload} 单独同步。 */
	public static void sendServerConfigsToPlayer(ServerPlayer player) {
		ConfigStore.getRegisteredFileKeys().forEach(key -> {
			var handler = ConfigStore.getHandler(key);
			if (handler.getType() != ConfigType.SERVER) return;
			String tomlContent;
			try {
				var typed = handler.cast();
				tomlContent = typed.getSerializer().writeToString(typed.getSavedConfig());
			} catch (SerializationException e) {
				LOGGER.error("Failed to serialize server config '{}'", key, e);
				return;
			}
			FlexConfigServices.NETWORK.sendToPlayer(player, new ServerConfigPayload(key.modId(), key.fileName(), tomlContent));
		});
	}
}
