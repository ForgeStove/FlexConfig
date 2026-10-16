package io.github.forgestove.flexconfig.server;
import io.github.forgestove.flexconfig.*;
import io.github.forgestove.flexconfig.api.ConfigType;
import io.github.forgestove.flexconfig.network.*;
import io.github.forgestove.flexconfig.platform.Platform;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;
/** 服务器侧的生命周期处理：SERVER 型配置的加载与热重载、配置锁的加载与下发（调用点由加载器的事件订阅转发）。 */
public final class ServerLifecycleHandler {
	private static int watchTicks;
	/** 世界即将开始：加载全部 SERVER 型配置，存档目录优先；双端都要跑，单人存档也走这里。 */
	public static void onServerAboutToStart(MinecraftServer server) {
		ConfigRegistry.loadServerConfigs(server);
	}
	public static void onServerTick() {
		if (++watchTicks % ConfigWatchService.INTERVAL_TICKS != 0) return;
		for (var key : ConfigWatchService.poll(Set.of(), ConfigType.SERVER)) {
			ConfigRegistry.getHandler(key).reloadAndCopy();
			ServerLockApplier.reapply(key);   // 重载会抹掉锁值，需要重新叠加
		}
	}
	public static void onServerStarting(MinecraftServer server) {
		ConfigRegistry.getRegisteredFileKeys().forEach(key -> {
			if (!ConfigRegistry.isLockable(key)) return;
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
		ConfigRegistry.applyWorldRestart();
		ConfigRegistry.unloadServerConfigs();
	}
	public static void onPlayerLogin(ServerPlayer player) {
		// 仅向安装了此模组的客户端发送 — 否则NeoForge会抛出异常。
		if (!Platform.get().canSendToPlayer(player, ConfigSyncPayload.TYPE)) return;
		ConfigNetwork.sendLocksToPlayer(player);
		ConfigNetwork.sendServerConfigsToPlayer(player);
	}
}
