package io.github.forgestove.flexconfig;
import io.github.forgestove.flexconfig.foundation.client.ClientLifecycleHandler;
import io.github.forgestove.flexconfig.foundation.network.ConfigNetwork;
import io.github.forgestove.flexconfig.foundation.server.ServerLifecycleHandler;
import io.github.forgestove.flexconfig.platform.NeoForgePayloads;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
@EventBusSubscriber
public class ConfigEvents {
	@SubscribeEvent
	public static void register(RegisterPayloadHandlersEvent event) {
		ConfigNetwork.register(NeoForgePayloads.create(event));
	}
	@SubscribeEvent
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		ServerLifecycleHandler.onServerAboutToStart(event.getServer());
	}
	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post event) {
		ServerLifecycleHandler.onServerTick();
	}
	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		ServerLifecycleHandler.onServerStopped();
	}
	@EventBusSubscriber(Dist.CLIENT)
	public static class Client {
		@SubscribeEvent
		public static void onClientTick(Post event) {
			ClientLifecycleHandler.tick();
		}
		@SubscribeEvent
		public static void onLoggingOut(LoggingOut event) {
			ClientLifecycleHandler.onLoggingOut();
		}
	}
	@EventBusSubscriber
	public static class Server {
		@SubscribeEvent
		public static void onServerStarting(ServerStartingEvent event) {
			ServerLifecycleHandler.onServerStarting(event.getServer());
		}
		@SubscribeEvent
		public static void onServerStopping(ServerStoppingEvent event) {
			ServerLifecycleHandler.onServerStopping();
		}
		@SubscribeEvent
		public static void onPlayerLogin(PlayerLoggedInEvent event) {
			if (event.getEntity() instanceof ServerPlayer player) ServerLifecycleHandler.onPlayerLogin(player);
		}
	}
}
