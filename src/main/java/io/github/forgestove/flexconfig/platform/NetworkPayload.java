package io.github.forgestove.flexconfig.platform;
import net.minecraft.network.FriendlyByteBuf;
//? if >=1.20.2 {
import io.github.forgestove.flexconfig.FlexConfig;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
//?}
/**
 * 网络包的最小面：频道名 + 写入缓冲。
 * <p>
 * 1.20.1 的 Forge 用 {@code SimpleChannel} 收发普通对象，没有 {@code CustomPacketPayload}/{@code StreamCodec}；
 * 用本接口隔开，平台实现各自决定怎么注册与发送。解码由各包自己提供的静态工厂完成。
 */
public interface NetworkPayload/*? if >=1.20.2 {*/ extends CustomPacketPayload/*?} */ {
	String channel();
	void encode(FriendlyByteBuf buf);
	//? if >=1.20.2 {
	@Override
	default Type<? extends CustomPacketPayload> type() {
		return new Type<>(ResourceLocation.fromNamespaceAndPath(FlexConfig.ID, channel()));
	}
	//?}
}
