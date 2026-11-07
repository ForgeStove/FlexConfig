package io.github.forgestove.flexconfig;
import io.github.forgestove.flexconfig.foundation.network.ConfigNetwork;
import io.github.forgestove.flexconfig.platform.*;
import io.github.forgestove.flexconfig.foundation.server.ServerLifecycleHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
/**
 * Fabric 的事件订阅适配层：本类只把事件数据解包后交给核心处理，不含业务逻辑。
 * <p>
 * 与 NeoForge 的 {@code ConfigEvents} 一一对应，区别是 NeoForge 分
 * {@code ServerAboutToStartEvent}（世界加载前）和 {@code ServerStartingEvent}（加载后）两次，
 * Fabric 只有 {@code SERVER_STARTING} 一次，所以两种事件都注册在这里。启动时配置已经就绪，提前应用锁不影响结果。
 */
public class FabricEvents implements ModInitializer {
	@Override
	public void onInitialize() {
		ConfigNetwork.register(new FabricPayloads());
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			FabricNetworkService.server = server;
			ServerLifecycleHandler.onServerAboutToStart(server);
			ServerLifecycleHandler.onServerStarting(server);
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> ServerLifecycleHandler.onServerTick());
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> ServerLifecycleHandler.onServerStopping());
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			FabricNetworkService.server = null;
			ServerLifecycleHandler.onServerStopped();
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> ServerLifecycleHandler.onPlayerLogin(handler.getPlayer()));
	}
}
