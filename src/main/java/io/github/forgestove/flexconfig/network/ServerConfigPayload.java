package io.github.forgestove.flexconfig.network;
import io.github.forgestove.flexconfig.platform.NetworkPayload;
import net.minecraft.network.FriendlyByteBuf;
/** 服务器把某个 SERVER 型配置的整份内容下发给客户端；客户端只存内存，不落盘。 */
public record ServerConfigPayload(String modId, String fileName, String tomlContent) implements NetworkPayload {
	public static final String CHANNEL = "server_config";
	public static ServerConfigPayload decode(FriendlyByteBuf buf) {
		return new ServerConfigPayload(buf.readUtf(), buf.readUtf(), buf.readUtf());
	}
	@Override
	public String channel() {
		return CHANNEL;
	}
	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeUtf(modId);
		buf.writeUtf(fileName);
		buf.writeUtf(tomlContent);
	}
}
