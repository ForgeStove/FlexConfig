package io.github.forgestove.flexconfig;
import io.github.forgestove.flexconfig.foundation.network.ConfigNetwork;
import io.github.forgestove.flexconfig.foundation.server.ServerLifecycleHandler;
import io.github.forgestove.flexconfig.platform.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
public class FabricEvents implements ModInitializer {
	@Override
	public void onInitialize() {
		ConfigNetwork.register(new FabricPayloads());
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			ServerLifecycleHandler.onServerAboutToStart(server);
			ServerLifecycleHandler.onServerStarting(server);
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> ServerLifecycleHandler.onServerTick());
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> ServerLifecycleHandler.onServerStopping());
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> ServerLifecycleHandler.onServerStopped());
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> ServerLifecycleHandler.onPlayerLogin(handler.getPlayer()));
	}
}
