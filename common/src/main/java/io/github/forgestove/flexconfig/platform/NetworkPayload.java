package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.foundation.common.CommonUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
public interface NetworkPayload extends CustomPacketPayload {
	String channel();
	void encode(FriendlyByteBuf buf);
	@Override
	default @NotNull Type<? extends CustomPacketPayload> type() {
		return new Type<>(CommonUtil.getFlexRes(channel()));
	}
}
