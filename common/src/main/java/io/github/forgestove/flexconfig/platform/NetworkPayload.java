package io.github.forgestove.flexconfig.platform;
import net.minecraft.network.FriendlyByteBuf;
public interface NetworkPayload {
	String channel();
	void encode(FriendlyByteBuf buf);
}
