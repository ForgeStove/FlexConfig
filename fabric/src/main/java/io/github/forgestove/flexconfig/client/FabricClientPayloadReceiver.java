package io.github.forgestove.flexconfig.client;
import io.github.forgestove.flexconfig.foundation.common.CommonUtil;
import io.github.forgestove.flexconfig.platform.*;
import io.github.forgestove.flexconfig.platform.services.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
public class FabricClientPayloadReceiver implements ClientPayloadReceiver {
	@Override
	public <P extends NetworkPayload> void serverToClient(
		String channel,
		Class<P> type,
		Function<FriendlyByteBuf, P> decoder,
		PayloadHandler<P> handler
	) {
		ClientPlayNetworking.registerGlobalReceiver(
			CommonUtil.getFlexRes(channel),
			(client, networkHandler, buf, sender) -> handler.handle(decoder.apply(buf), new FabricClientContext(client))
		);
	}
	/** 客户端侧实现；客户端没有服务端玩家，所以 {@link #player()} 恒为 {@code null}。 */
	private record FabricClientContext(Minecraft client) implements PayloadContext {
		@Override
		public @Nullable ServerPlayer player() {
			return null;
		}
		@Override
		public void enqueue(Runnable task) {
			client.execute(task);
		}
	}
}
