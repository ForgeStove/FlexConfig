package io.github.forgestove.flexconfig.client;
import io.github.forgestove.flexconfig.FlexConfig;
import io.github.forgestove.flexconfig.platform.NetworkPayload;
import io.github.forgestove.flexconfig.platform.services.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
public class FabricClientPayloadReceiver implements ClientPayloadReceiver {
	@Override
	public <P extends NetworkPayload> void serverToClient(String channel, Class<P> type, PayloadHandler<P> handler) {
		var registered = new Type<P>(FlexConfig.getFlexRes(channel));
		ClientPlayNetworking.registerGlobalReceiver(
			registered,
			(payload, context) -> handler.handle(payload, new FabricClientContext(context))
		);
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
