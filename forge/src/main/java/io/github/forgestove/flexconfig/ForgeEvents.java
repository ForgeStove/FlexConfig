package io.github.forgestove.flexconfig;
import io.github.forgestove.flexconfig.foundation.client.ClientLifecycleHandler;
import io.github.forgestove.flexconfig.foundation.network.ConfigNetwork;
import io.github.forgestove.flexconfig.foundation.server.ServerLifecycleHandler;
import io.github.forgestove.flexconfig.platform.ForgePayloads;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.minecraftforge.event.TickEvent.*;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.server.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
@EventBusSubscriber(modid = FlexConfig.ID)
public class ForgeEvents {
	@SubscribeEvent
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		ServerLifecycleHandler.onServerAboutToStart(event.getServer());
	}
	@SubscribeEvent
	public static void onServerTick(ServerTickEvent event) {
		if (event.phase == Phase.END) ServerLifecycleHandler.onServerTick();
	}
	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		ServerLifecycleHandler.onServerStopped();
	}
	@EventBusSubscriber(modid = FlexConfig.ID, bus = Bus.MOD)
	public static class Setup {
		@SubscribeEvent
		public static void onCommonSetup(FMLCommonSetupEvent event) {
			event.enqueueWork(() -> ConfigNetwork.register(new ForgePayloads()));
		}
	}
	@EventBusSubscriber(modid = FlexConfig.ID, value = Dist.CLIENT)
	public static class Client {
		@SubscribeEvent
		public static void onClientTick(ClientTickEvent event) {
			if (event.phase == Phase.END) ClientLifecycleHandler.tick();
		}
		@SubscribeEvent
		public static void onLoggingOut(LoggingOut event) {
			ClientLifecycleHandler.onLoggingOut();
		}
	}
	@EventBusSubscriber(modid = FlexConfig.ID)
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
