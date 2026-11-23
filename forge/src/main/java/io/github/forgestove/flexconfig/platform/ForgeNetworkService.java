package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.platform.services.NetworkService;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;
public class ForgeNetworkService implements NetworkService {
	@Override
	public void sendToPlayer(ServerPlayer player, NetworkPayload payload) {
		ForgePayloads.channel(payload.channel()).send(PacketDistributor.PLAYER.with(() -> player), payload);
	}
	@Override
	public void sendToAllPlayers(MinecraftServer server, NetworkPayload payload) {
		ForgePayloads.channel(payload.channel()).send(PacketDistributor.ALL.noArg(), payload);
	}
	@Override
	public void sendToServer(NetworkPayload payload) {
		ForgePayloads.channel(payload.channel()).sendToServer(payload);
	}
	@Override
	public boolean canSendToPlayer(ServerPlayer player, String channel) {
		var channelHolder = ForgePayloads.find(channel);
		return channelHolder != null && channelHolder.isRemotePresent(player.connection.connection);
	}
}
