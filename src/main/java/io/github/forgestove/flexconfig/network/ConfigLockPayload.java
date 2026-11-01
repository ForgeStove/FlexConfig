package io.github.forgestove.flexconfig.network;
import io.github.forgestove.flexconfig.platform.NetworkPayload;
import net.minecraft.network.FriendlyByteBuf;
/** 向服务端发送的锁定请求：{@code value} 为空串表示解锁，否则为字符串形式的锁定值。 */
public record ConfigLockPayload(String modId, String fileName, String configId, String value) implements NetworkPayload {
	public static final String CHANNEL = "config_lock";
	public static ConfigLockPayload decode(FriendlyByteBuf buf) {
		return new ConfigLockPayload(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf());
	}
	@Override
	public String channel() {
		return CHANNEL;
	}
	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeUtf(modId);
		buf.writeUtf(fileName);
		buf.writeUtf(configId);
		buf.writeUtf(value);
	}
}
