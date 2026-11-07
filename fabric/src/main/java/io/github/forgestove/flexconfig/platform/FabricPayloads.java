package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.FlexConfig;
import io.github.forgestove.flexconfig.platform.services.PayloadContext;
import io.github.forgestove.flexconfig.platform.services.PayloadHandler;
import io.github.forgestove.flexconfig.platform.services.PayloadRegistrar;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
/**
 * Fabric 的包注册。S2C 接收器只能用 {@code ClientPlayNetworking} 注册，这个类只在客户端存在，
 * 所以要先把接收器暂存，等 {@code FabricClientEvents} 在 {@code onInitializeClient} 里注册。
 * <p>
 * 类型注册（{@code PayloadTypeRegistry}）和服务端接收器在两端都能用，在 common 的初始化阶段就能完成。
 */
public class FabricPayloads implements PayloadRegistrar {
	/** 待注册的 S2C 接收器。 */
	private static final List<Pending<?>> PENDING = new ArrayList<>();
	/** 客户端初始化时调用，把暂存的 S2C 接收器注册到 {@code ClientPlayNetworking}。 */
	public static void registerClientReceivers() {
		PENDING.forEach(FabricPayloads::registerClient);
		PENDING.clear();
	}
	private static <P extends NetworkPayload> void registerClient(Pending<P> pending) {
		ClientPlayNetworking.registerGlobalReceiver(
			pending.type(),
			(payload, context) -> pending.handler().handle(payload, new FabricClientContext(context))
		);
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
		PENDING.add(new Pending<>(registered, handler));
	}
	private record Pending<P extends NetworkPayload>(Type<P> type, PayloadHandler<P> handler) {}
	/** 服务端侧实现；接收器回调在网络线程，用 {@code server().execute} 切回主线程。 */
	private record FabricServerContext(ServerPlayNetworking.Context context) implements PayloadContext {
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
	/** 客户端侧实现；客户端没有服务端玩家，所以 {@link #player()} 恒为 {@code null}。 */
	private record FabricClientContext(Context context) implements PayloadContext {
		@Override
		public @Nullable ServerPlayer player() {
			return null;
		}
		@SuppressWarnings("resource")
		@Override
		public void enqueue(Runnable task) {
			context.client().execute(task);
		}
	}
}
