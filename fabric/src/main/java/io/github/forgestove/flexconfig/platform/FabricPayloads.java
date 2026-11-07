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
 * Fabric 的包注册。与 NeoForge 的单入口不同：S2C 的接收器只能用 {@code ClientPlayNetworking} 注册，
 * 那是客户端专属类，只能等到客户端入口再碰——所以这里先把 S2C 接收器暂存，
 * 由 {@code FabricClientEvents} 在 {@code onInitializeClient} 里取走注册。
 * <p>
 * 类型注册（{@code PayloadTypeRegistry}）与服务端接收器是双端 API，在 common 入口即可完成。
 */
public class FabricPayloads implements PayloadRegistrar {
	/** 待注册的 S2C 接收器。 */
	private static final List<Pending<?>> PENDING = new ArrayList<>();
	/** 由客户端入口调用：把暂存的 S2C 接收器真正挂到 {@code ClientPlayNetworking} 上。 */
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
	/** 服务端上下文；接收器回调在网络线程，用 {@code server().execute} 切回主线程。 */
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
	/** 客户端上下文；客户端没有「服务端玩家」，故 {@link #player()} 恒为 {@code null}。 */
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
