package io.github.forgestove.flexconfig.network;
import io.github.forgestove.flexconfig.platform.NetworkPayload;
import net.minecraft.network.FriendlyByteBuf;
public record ConfigSyncPayload(String modId, String fileName, String tomlContent) implements NetworkPayload {
	public static final String CHANNEL = "config_sync";
	public static ConfigSyncPayload decode(FriendlyByteBuf buf) {
		return new ConfigSyncPayload(buf.readUtf(), buf.readUtf(), buf.readUtf());
	}
	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeUtf(modId);
		buf.writeUtf(fileName);
		buf.writeUtf(tomlContent);
	}
}
