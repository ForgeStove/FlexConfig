package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.FlexConfig;
import io.github.forgestove.flexconfig.platform.services.*;
import net.fabricmc.fabric.api.networking.v1.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
/**
 * Fabric 的包注册。C2S 类型、服务端接收器和 S2C 类型在这里注册；
 * S2C 接收器依赖客户端类，由 {@code FabricClientPayloadReceiver} 在客户端初始化时注册。
 */
public class FabricPayloads implements PayloadRegistrar {
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
		PayloadTypeRegistry.playC2S().register(registered, codec(decoder));
		ServerPlayNetworking.registerGlobalReceiver(
			registered,
			(payload, context) -> handler.handle(payload, new FabricServerContext(context))
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
		PayloadTypeRegistry.playS2C().register(registered, codec(decoder));
	}
	/** 服务端侧实现；接收器回调在网络线程，用 {@code server().execute} 切回主线程。 */
	private record FabricServerContext(Context context) implements PayloadContext {
		@Override
		public @Nullable ServerPlayer player() {
			return context.player();
		}
		@SuppressWarnings("resource")
		@Override
		public void enqueue(Runnable task) {
			context.server().execute(task);
		}
	}
}
