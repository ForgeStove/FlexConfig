package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.FlexConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.*;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.*;
/** {@link Platform} 的 NeoForge 实现。 */
public final class NeoForgePlatform implements Platform {
	/** 由 {@link RegisterPayloadHandlersEvent} 的订阅者调用，把加载器的注册器交给核心。 */
	public static void registerPayloads(RegisterPayloadHandlersEvent event, Consumer<Payloads> consumer) {
		consumer.accept(new NeoForgePayloads(event.registrar(FlexConfig.NETWORK_VERSION).optional()));
	}
	@Override
	public Path configDir() {
		return FMLPaths.CONFIGDIR.get();
	}
	@Override
	public boolean isModLoaded(String modId) {
		return LoadingModList.get().getModFileById(modId) != null;
	}
	@Override
	public boolean isClient() {
		return FMLEnvironment.dist.isClient();
	}
	@Override
	public boolean isDevelopmentEnvironment() {
		return !FMLLoader.isProduction();
	}
	@Override
	public @Nullable String modDisplayName(String modId) {
		return ModList.get().getModContainerById(modId).map(container -> container.getModInfo().getDisplayName()).orElse(null);
	}
	@Override
	public void registerConfigScreen(String modId, Supplier<Screen> screenFactory) {
		ModList.get().getModContainerById(modId).ifPresent(container -> {
			Supplier<IConfigScreenFactory> extension = () -> (modContainer, screen) -> screenFactory.get();
			container.registerExtensionPoint(IConfigScreenFactory.class, extension);
		});
	}
	@Override
	public void sendToPlayer(ServerPlayer player, NetworkPayload payload) {
		PacketDistributor.sendToPlayer(player, payload);
	}
	@Override
	public void sendToAllPlayers(NetworkPayload payload) {
		PacketDistributor.sendToAllPlayers(payload);
	}
	@Override
	public void sendToServer(NetworkPayload payload) {
		PacketDistributor.sendToServer(payload);
	}
	@Override
	public boolean canSendToPlayer(ServerPlayer player, String channel) {
		return player.connection.hasChannel(new Type<>(FlexConfig.getFlexRes(channel)));
	}
	private record NeoForgePayloads(PayloadRegistrar registrar) implements Payloads {
		private static <P extends NetworkPayload> StreamCodec<FriendlyByteBuf, P> codec(Function<FriendlyByteBuf, P> decoder) {
			return StreamCodec.of((buf, payload) -> payload.encode(buf), decoder::apply);
		}
		@Override
		public <P extends NetworkPayload> void clientToServer(
			String channel,
			Class<P> type,
			Function<FriendlyByteBuf, P> decoder,
			PayloadHandler<P> handler
		) {
			var registered = new Type<P>(FlexConfig.getFlexRes(channel));
			registrar.playToServer(
				registered,
				codec(decoder),
				(payload, context) -> handler.handle(payload, new NeoForgeContext(context))
			);
		}
		@Override
		public <P extends NetworkPayload> void serverToClient(
			String channel,
			Class<P> type,
			Function<FriendlyByteBuf, P> decoder,
			PayloadHandler<P> handler
		) {
			var registered = new Type<P>(FlexConfig.getFlexRes(channel));
			registrar.playToClient(
				registered,
				codec(decoder),
				(payload, context) -> handler.handle(payload, new NeoForgeContext(context))
			);
		}
	}
	private record NeoForgeContext(IPayloadContext context) implements PayloadContext {
		@Override
		public @Nullable ServerPlayer player() {
			return context.player() instanceof ServerPlayer player ? player : null;
		}
		@Override
		public void enqueue(Runnable task) {
			context.enqueueWork(task);
		}
	}
}
