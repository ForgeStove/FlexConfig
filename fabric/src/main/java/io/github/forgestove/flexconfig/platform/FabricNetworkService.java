package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.foundation.common.CommonUtil;
import io.github.forgestove.flexconfig.platform.services.NetworkService;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
public class FabricNetworkService implements NetworkService {
	private static FriendlyByteBuf buffer(NetworkPayload payload) {
		var buf = PacketByteBufs.create();
		payload.encode(buf);
		return buf;
	}
	@Override
	public void sendToPlayer(ServerPlayer player, NetworkPayload payload) {
		ServerPlayNetworking.send(player, CommonUtil.getFlexRes(payload.channel()), buffer(payload));
	}
	@Override
	public void sendToAllPlayers(MinecraftServer server, NetworkPayload payload) {
		var id = CommonUtil.getFlexRes(payload.channel());
		for (var player : server.getPlayerList().getPlayers()) ServerPlayNetworking.send(player, id, buffer(payload));
	}
	/** 客户端专属 API，用全限定名写，JVM 执行到这里时才解析，专用服务器不会调用。 */
	@Override
	public void sendToServer(NetworkPayload payload) {
		ClientPlayNetworking.send(CommonUtil.getFlexRes(payload.channel()), buffer(payload));
	}
	@Override
	public boolean canSendToPlayer(ServerPlayer player, String channel) {
		return ServerPlayNetworking.canSend(player, CommonUtil.getFlexRes(channel));
	}
}
