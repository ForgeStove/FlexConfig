package io.github.forgestove.flexconfig.platform.services;
import io.github.forgestove.flexconfig.platform.NetworkPayload;
import net.minecraft.server.level.ServerPlayer;
/** 发送配置同步包；{@link #sendToServer} 只能在客户端调用。 */
public interface NetworkService {
	void sendToPlayer(ServerPlayer player, NetworkPayload payload);
	void sendToAllPlayers(NetworkPayload payload);
	void sendToServer(NetworkPayload payload);
	/** 对方客户端没装本模组时不能发，否则加载器会抛异常。 */
	boolean canSendToPlayer(ServerPlayer player, String channel);
}
