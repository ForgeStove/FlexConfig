package io.github.forgestove.flexconfig.foundation.server;
import io.github.forgestove.flexconfig.api.ConfigType;
import io.github.forgestove.flexconfig.foundation.ConfigStore;
import io.github.forgestove.flexconfig.foundation.ConfigWatchService;
import io.github.forgestove.flexconfig.foundation.network.*;
import io.github.forgestove.flexconfig.platform.FlexConfigServices;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;
/** 服务端启动、停止和玩家登录时调用这里的方法，处理 SERVER 型配置的加载、热重载和配置锁下发。 */
public class ServerLifecycleHandler {
	private static int watchTicks;
	/** 世界开始前加载全部 SERVER 型配置，存档目录优先；客户端和服务端都要调用，单人存档也会进入这里。 */
	public static void onServerAboutToStart(MinecraftServer server) {
		ConfigStore.loadServerConfigs(server);
	}
	public static void onServerTick() {
		if (++watchTicks % ConfigWatchService.INTERVAL_TICKS != 0) return;
		for (var key : ConfigWatchService.poll(Set.of(), ConfigType.SERVER)) {
			ConfigStore.getHandler(key).reloadAndCopy();
			ServerLockApplier.reapply(key);   // 重载会抹掉锁值，需要重新叠加
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
	/** 顺序不可颠倒：WORLD 型改动要在 SERVER 型配置卸载前生效。 */
	public static void onServerStopped() {
		ConfigStore.applyWorldRestart();
		ConfigStore.unloadServerConfigs();
	}
	public static void onPlayerLogin(ServerPlayer player) {
		// 仅向安装了此模组的客户端发送 — 否则NeoForge会抛出异常。
		if (!FlexConfigServices.NETWORK.canSendToPlayer(player, ConfigSyncPayload.CHANNEL)) return;
		ConfigNetwork.sendLocksToPlayer(player);
		ConfigNetwork.sendServerConfigsToPlayer(player);
	}
}
