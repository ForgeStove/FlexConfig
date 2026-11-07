package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.FlexConfig;
import io.github.forgestove.flexconfig.platform.services.NetworkService;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
public class FabricNetworkService implements NetworkService {
	@Override
	public void sendToPlayer(ServerPlayer player, NetworkPayload payload) {
		ServerPlayNetworking.send(player, payload);
	}
	@Override
	public void sendToAllPlayers(MinecraftServer server, NetworkPayload payload) {
		for (var player : server.getPlayerList().getPlayers()) ServerPlayNetworking.send(player, payload);
	}
	/** 客户端专属 API，用全限定名写，JVM 执行到这里时才解析，专用服务器不会调用。 */
	@Override
	public void sendToServer(NetworkPayload payload) {
		ClientPlayNetworking.send(payload);
	}
	@Override
	public boolean canSendToPlayer(ServerPlayer player, String channel) {
		return ServerPlayNetworking.canSend(player, new Type<>(FlexConfig.getFlexRes(channel)));
	}
}
