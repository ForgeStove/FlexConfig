package io.github.forgestove.flexconfig.foundation.server;
import io.github.forgestove.flexconfig.api.ConfigType;
import io.github.forgestove.flexconfig.foundation.*;
import io.github.forgestove.flexconfig.foundation.network.*;
import io.github.forgestove.flexconfig.platform.FlexConfigServices;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;
public class ServerLifecycleHandler {
	private static int watchTicks;
	public static void onServerAboutToStart(MinecraftServer server) {
		ConfigStore.loadServerConfigs(server);
	}
	public static void onServerTick() {
		if (++watchTicks % ConfigWatchService.INTERVAL_TICKS != 0) return;
		for (var key : ConfigWatchService.poll(Set.of(), ConfigType.SERVER)) {
			ConfigStore.getHandler(key).reloadAndCopy();
			ServerLockApplier.reapply(key);
		}
	}
	public static void onServerStarting(MinecraftServer server) {
		ConfigStore.getRegisteredFileKeys().forEach(key -> {
			if (!ConfigStore.isLockable(key)) return;
			var lockStore = new ServerConfigLockStore(server, key.modId());
			lockStore.load();
			ConfigNetwork.setLockStore(key, lockStore);
			ServerLockApplier.applyAll(key, lockStore);
		});
	}
	public static void onServerStopping() {
		ConfigNetwork.saveAll();
		ConfigNetwork.clearAll();
	}
	public static void onServerStopped() {
		ConfigStore.applyWorldRestart();
		ConfigStore.unloadServerConfigs();
	}
	public static void onPlayerLogin(ServerPlayer player) {
		// 仅向安装了此模组的客户端发送
		if (!FlexConfigServices.NETWORK.canSendToPlayer(player, ConfigSyncPayload.CHANNEL)) return;
		ConfigNetwork.sendLocksToPlayer(player);
		ConfigNetwork.sendServerConfigsToPlayer(player);
	}
}
