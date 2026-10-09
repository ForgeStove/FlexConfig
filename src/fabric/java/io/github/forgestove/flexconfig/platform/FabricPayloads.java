package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.platform.Platform.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.*;
/**
 * Fabric 的包注册。与 NeoForge 的单入口不同：S2C 的接收器只能用 {@code ClientPlayNetworking} 注册，
 * 那是客户端专属类，只能等到客户端入口再碰——所以这里先把 S2C 接收器暂存，
 * 由 {@code FabricClientEvents} 在 {@code onInitializeClient} 里取走注册。
 * <p>
 * 类型注册（{@code PayloadTypeRegistry}）与服务端接收器是双端 API，在 common 入口即可完成。
 */
public final class FabricPayloads implements Payloads {
	/** 待注册的 S2C 接收器。 */
	private static final List<Pending<?>> PENDING = new ArrayList<>();
	/** 由客户端入口调用：把暂存的 S2C 接收器真正挂到 {@code ClientPlayNetworking} 上。 */
	public static void registerClientReceivers() {
		PENDING.forEach(FabricPayloads::registerClient);
		PENDING.clear();
	}
	private static <P extends CustomPacketPayload> void registerClient(Pending<P> pending) {
		ClientPlayNetworking.registerGlobalReceiver(
			pending.type(),
			(payload, context) -> pending.handler().handle(payload, new FabricClientContext(context))
		);
	}
	@Override
	public <P extends CustomPacketPayload> void clientToServer(
		Type<P> type,
		StreamCodec<FriendlyByteBuf, P> codec,
		PayloadHandler<P> handler
	) {
		PayloadTypeRegistry.playC2S().register(type, codec);
		ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.handle(payload, new FabricServerContext(context)));
	}
	@Override
	public <P extends CustomPacketPayload> void serverToClient(
		Type<P> type,
		StreamCodec<FriendlyByteBuf, P> codec,
		PayloadHandler<P> handler
	) {
		PayloadTypeRegistry.playS2C().register(type, codec);
		PENDING.add(new Pending<>(type, handler));
	}
	private record Pending<P extends CustomPacketPayload>(Type<P> type, PayloadHandler<P> handler) {}
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
