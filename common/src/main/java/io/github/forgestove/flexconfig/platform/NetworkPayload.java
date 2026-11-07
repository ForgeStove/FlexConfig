package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.FlexConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
/**
 * 各加载器的网络包共用这个接口：{@link #type()} 用 flexconfig 命名空间和 channel 生成包 ID，解码由各包自己的静态工厂完成。
 */
public interface NetworkPayload extends CustomPacketPayload {
	String channel();
	void encode(FriendlyByteBuf buf);
	@Override
	default @NotNull Type<? extends CustomPacketPayload> type() {
		return new Type<>(FlexConfig.getFlexRes(channel()));
	}
}
