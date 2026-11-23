package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.foundation.common.CommonUtil;
import io.github.forgestove.flexconfig.platform.services.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Function;
/**
 * Fabric 1.20.1 的包注册。C2S 接收器在这里注册；S2C 接收器依赖客户端类，
 * 由 {@code FabricClientPayloadReceiver} 在客户端初始化时注册。
 */
public class FabricPayloads implements PayloadRegistrarService {
	@Override
	public <P extends NetworkPayload> void clientToServer(
		String channel,
		Class<P> type,
		Function<FriendlyByteBuf, P> decoder,
		PayloadHandler<P> handler
	) {
		ServerPlayNetworking.registerGlobalReceiver(
			CommonUtil.getFlexRes(channel),
			(server, player, networkHandler, buf, sender) -> handler.handle(decoder.apply(buf), new FabricServerContext(server, player))
		);
	}
	@Override
	public <P extends NetworkPayload> void serverToClient(
		String channel,
		Class<P> type,
		Function<FriendlyByteBuf, P> decoder,
		PayloadHandler<P> handler
	) {
		// 1.20.1 没有可复用的包类型注册表，S2C 接收器只能在客户端初始化时注册
	}
	/** 服务端侧实现；接收器回调在网络线程，用 {@code server.execute} 切回主线程。 */
	private record FabricServerContext(MinecraftServer server, ServerPlayer player) implements PayloadContext {
		@Override
		public void enqueue(Runnable task) {
			server.execute(task);
		}
	}
}
