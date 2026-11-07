package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.FlexConfig;
import io.github.forgestove.flexconfig.platform.services.NetworkService;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
public class NeoForgeNetworkService implements NetworkService {
	@Override
	public void sendToPlayer(ServerPlayer player, NetworkPayload payload) {
		PacketDistributor.sendToPlayer(player, payload);
	}
	@Override
	public void sendToAllPlayers(MinecraftServer server, NetworkPayload payload) {
		PacketDistributor.sendToAllPlayers(payload);
	}
	@Override
	public void sendToServer(NetworkPayload payload) {
		PacketDistributor.sendToServer(payload);
	}
	@Override
	public boolean canSendToPlayer(ServerPlayer player, String channel) {
		return player.connection.hasChannel(new Type<>(FlexConfig.getFlexRes(channel)));
	}
}
