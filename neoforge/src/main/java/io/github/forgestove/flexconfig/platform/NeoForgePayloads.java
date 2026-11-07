package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.FlexConfig;
import io.github.forgestove.flexconfig.platform.services.PayloadContext;
import io.github.forgestove.flexconfig.platform.services.PayloadHandler;
import io.github.forgestove.flexconfig.platform.services.PayloadRegistrar;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
public record NeoForgePayloads(net.neoforged.neoforge.network.registration.PayloadRegistrar registrar) implements PayloadRegistrar {
	public static NeoForgePayloads create(RegisterPayloadHandlersEvent event) {
		return new NeoForgePayloads(event.registrar(FlexConfig.NETWORK_VERSION).optional());
	}
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
		registrar.playToServer(registered, codec(decoder), (payload, context) -> handler.handle(payload, new NeoForgeContext(context)));
	}
	@Override
	public <P extends NetworkPayload> void serverToClient(
		String channel,
		Class<P> type,
		Function<FriendlyByteBuf, P> decoder,
		PayloadHandler<P> handler
	) {
		var registered = new Type<P>(FlexConfig.getFlexRes(channel));
		registrar.playToClient(registered, codec(decoder), (payload, context) -> handler.handle(payload, new NeoForgeContext(context)));
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
