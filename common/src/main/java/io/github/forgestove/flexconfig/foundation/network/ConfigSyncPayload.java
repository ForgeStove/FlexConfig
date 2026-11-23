package io.github.forgestove.flexconfig.foundation.network;
import io.github.forgestove.flexconfig.platform.NetworkPayload;
import net.minecraft.network.FriendlyByteBuf;
/** 服务器把某个配置文件的锁定条目作为 TOML 分区广播给全体客户端。 */
public record ConfigSyncPayload(String modId, String fileName, String tomlContent) implements NetworkPayload {
	public static final String CHANNEL = "config_sync";
	public static ConfigSyncPayload decode(FriendlyByteBuf buf) {
		return new ConfigSyncPayload(buf.readUtf(), buf.readUtf(), buf.readUtf());
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
