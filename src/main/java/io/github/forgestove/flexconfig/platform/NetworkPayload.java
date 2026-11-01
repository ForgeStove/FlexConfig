package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.FlexConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
/**
 * 网络包的最小面：频道名 + 写入缓冲。
 * <p>
 * 1.20.1 的 Forge 用 {@code SimpleChannel} 收发普通对象，没有 {@code CustomPacketPayload}/{@code StreamCodec}；
 * 用本接口隔开，平台实现各自决定怎么注册与发送。解码由各包自己提供的静态工厂完成。
 */
public interface NetworkPayload extends CustomPacketPayload {
	String channel();
	void encode(FriendlyByteBuf buf);
	@Override
	default @NotNull Type<? extends CustomPacketPayload> type() {
		return new Type<>(FlexConfig.getFlexRes(channel()));
	}
}
