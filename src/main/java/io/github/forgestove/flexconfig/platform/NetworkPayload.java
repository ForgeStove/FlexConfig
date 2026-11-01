package io.github.forgestove.flexconfig.platform;
import net.minecraft.network.FriendlyByteBuf;
public interface NetworkPayload {
	void encode(FriendlyByteBuf buf);
}
