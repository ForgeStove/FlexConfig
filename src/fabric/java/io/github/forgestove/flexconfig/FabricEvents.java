package io.github.forgestove.flexconfig;
import io.github.forgestove.flexconfig.network.ConfigNetwork;
import io.github.forgestove.flexconfig.platform.*;
import io.github.forgestove.flexconfig.server.ServerLifecycleHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
/**
 * Fabric 的事件订阅适配层：本类只做「解包事件数据 → 转交核心」，不含任何业务逻辑。
 * <p>
 * 与 NeoForge 的 {@code ConfigEvents} 一一对应，唯一的语义收缩：NeoForge 分
 * {@code ServerAboutToStartEvent}（世界加载前）与 {@code ServerStartingEvent}（加载后）两档，
 * Fabric 只有 {@code SERVER_STARTING} 一档，故两者都压到这里。注册期配置已就绪，早一点应用锁无害。
 */
public final class FabricEvents implements ModInitializer {
	@Override
	public void onInitialize() {
		ConfigNetwork.register(new FabricPayloads());
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			FabricPlatform.server = server;
			ServerLifecycleHandler.onServerAboutToStart(server);
			ServerLifecycleHandler.onServerStarting(server);
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> ServerLifecycleHandler.onServerTick());
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> ServerLifecycleHandler.onServerStopping());
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			FabricPlatform.server = null;
			ServerLifecycleHandler.onServerStopped();
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> ServerLifecycleHandler.onPlayerLogin(handler.getPlayer()));
	}
}
