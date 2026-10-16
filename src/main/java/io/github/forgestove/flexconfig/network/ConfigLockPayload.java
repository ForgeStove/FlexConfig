package io.github.forgestove.flexconfig.network;
import io.github.forgestove.flexconfig.FlexConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
/** 向服务端发送的锁定请求：{@code value} 为空串表示解锁，否则为字符串形式的锁定值。 */
public record ConfigLockPayload(String modId, String fileName, String configId, String value) implements CustomPacketPayload {
	public static final Type<ConfigLockPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FlexConfig.ID, "config_lock"));
	public static final StreamCodec<FriendlyByteBuf, ConfigLockPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8,
		ConfigLockPayload::modId,
		ByteBufCodecs.STRING_UTF8,
		ConfigLockPayload::fileName,
		ByteBufCodecs.STRING_UTF8,
		ConfigLockPayload::configId,
		ByteBufCodecs.STRING_UTF8,
		ConfigLockPayload::value,
		ConfigLockPayload::new
	);
	@Override
	public @NotNull Type<ConfigLockPayload> type() {
		return TYPE;
	}
}
