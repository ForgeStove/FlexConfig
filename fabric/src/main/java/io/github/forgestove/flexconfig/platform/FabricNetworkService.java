package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.FlexConfig;
import io.github.forgestove.flexconfig.platform.services.NetworkService;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
public class FabricNetworkService implements NetworkService {
	/** 当前服务端实例；由 {@code FabricEvents} 在启动/停止时维护，只在广播时使用（Fabric 没有全局广播 API）。 */
	public static MinecraftServer server;
	@Override
	public void sendToPlayer(ServerPlayer player, NetworkPayload payload) {
		ServerPlayNetworking.send(player, payload);
	}
	@Override
	public void sendToAllPlayers(NetworkPayload payload) {
		var current = server;
		if (current == null) return;
		for (var player : current.getPlayerList().getPlayers()) ServerPlayNetworking.send(player, payload);
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
