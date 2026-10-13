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
	/** 世界即将开始：加载全部 SERVER 型配置（存档目录优先）。双端都要跑，单人存档也走这里。 */
	public static void onServerAboutToStart(MinecraftServer server) {
		ConfigRegistry.loadServerConfigs(server);
	}
	/** 服务端 tick：SERVER 型配置的外部改动热重载。双端都要跑，单人存档也走这里。 */
	public static void onServerTick() {
		if (++watchTicks % ConfigWatchService.INTERVAL_TICKS != 0) return;
		for (var key : ConfigWatchService.poll(Set.of(), ConfigType.SERVER)) {
			ConfigRegistry.getHandler(key).reloadAndCopy();
			ServerLockApplier.reapply(key);   // 重载会抹掉锁值，需要重新叠加
		}
	}
	/** 服务器启动时：为每份「可锁定」的配置（非 SERVER 型）从磁盘加载锁定存储。 */
	public static void onServerStarting(MinecraftServer server) {
		ConfigRegistry.getRegisteredFileKeys().forEach(key -> {
			if (!ConfigRegistry.isLockable(key)) return;
			var lockStore = new ServerConfigLockStore(server, key.modId());
			lockStore.load();
			ConfigNetwork.setLockStore(key, lockStore);
			ServerLockApplier.applyAll(key, lockStore);
		});
	}
	/** 服务器停止时：将所有模组的锁定存储保存到磁盘。 */
	public static void onServerStopping() {
		ConfigNetwork.saveAll();
		ConfigNetwork.clearAll();
	}
	/** 世界已停止：先让 WORLD 型改动生效，再卸载 SERVER 型配置。 */
	public static void onServerStopped() {
		ConfigRegistry.applyWorldRestart();
		ConfigRegistry.unloadServerConfigs();
	}
	/** 玩家登录时：向该玩家发送所有模组当前锁定的配置与 SERVER 型配置（如果他们安装了该模组）。 */
	public static void onPlayerLogin(ServerPlayer player) {
		// 仅向安装了此模组的客户端发送 — 否则NeoForge会抛出异常。
		if (!Platform.get().canSendToPlayer(player, ConfigSyncPayload.TYPE)) return;
		ConfigNetwork.sendLocksToPlayer(player);
		ConfigNetwork.sendServerConfigsToPlayer(player);
	}
}
