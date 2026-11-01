package io.github.forgestove.flexconfig;
import io.github.forgestove.flexconfig.client.ClientLifecycleHandler;
import io.github.forgestove.flexconfig.network.ConfigNetwork;
import io.github.forgestove.flexconfig.platform.ForgePlatform;
import io.github.forgestove.flexconfig.server.ServerLifecycleHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.minecraftforge.client.event.ScreenEvent.MouseButtonPressed.Pre;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.server.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
/**
 * Forge 的事件订阅适配层：本文件只做「解包事件数据 → 转交核心」，不含任何业务逻辑。
 * <p>
 * 与 1.21.1 的 {@code ConfigEvents} 对应，差异在 tick 事件要自己判断 {@code Phase.END}，
 * 以及网络注册走 {@link FMLCommonSetupEvent}（1.20.1 没有 payload 注册事件）。
 */
@EventBusSubscriber(modid = FlexConfig.ID)
public final class ForgeEvents {
	@SubscribeEvent
	public static void onCommonSetup(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> ForgePlatform.registerPayloads(ConfigNetwork::register));
	}
	@SubscribeEvent
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		ServerLifecycleHandler.onServerAboutToStart(event.getServer());
	}
	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) ServerLifecycleHandler.onServerTick();
	}
	@SubscribeEvent
	public static void onServerStopped(ServerStoppedEvent event) {
		ServerLifecycleHandler.onServerStopped();
	}
	@EventBusSubscriber(modid = FlexConfig.ID, value = Dist.CLIENT)
	public static class Client {
		@SubscribeEvent
		public static void register(Pre event) {
			ClientLifecycleHandler.onScreenMousePressed(event.getMouseX(), event.getMouseY());
		}
		@SubscribeEvent
		public static void onClientTick(TickEvent.ClientTickEvent event) {
			if (event.phase == TickEvent.Phase.END) ClientLifecycleHandler.tick();
		}
		@SubscribeEvent
		public static void onLoggingOut(LoggingOut event) {
			ClientLifecycleHandler.onLoggingOut();
		}
	}
	/**
	 * 服务端事件两种 dist 都要注册：单人存档的内置服务端就跑在客户端 JVM 里。
	 * 只挂 {@code Dist.DEDICATED_SERVER} 的话，内置服务端上锁存储不会加载、加入的玩家也收不到锁与 SERVER 配置。
	 */
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
