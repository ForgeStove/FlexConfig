package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.FlexConfig;
import io.github.forgestove.flexconfig.foundation.common.CommonUtil;
import io.github.forgestove.flexconfig.platform.services.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
public class ForgePayloads implements PayloadRegistrarService {
	private static final Map<String, SimpleChannel> CHANNELS = new HashMap<>();
	static SimpleChannel channel(String name) {
		return CHANNELS.computeIfAbsent(
			name,
			key -> NetworkRegistry.newSimpleChannel(
				CommonUtil.getFlexRes(key),
				() -> FlexConfig.NETWORK_VERSION,
				FlexConfig.NETWORK_VERSION::equals,
				FlexConfig.NETWORK_VERSION::equals
			)
		);
	}
	static @Nullable SimpleChannel find(String name) {
		return CHANNELS.get(name);
	}
	@Override
	public <P extends NetworkPayload> void clientToServer(
		String channel,
		Class<P> type,
		Function<FriendlyByteBuf, P> decoder,
		PayloadHandler<P> handler
	) {
		register(channel, type, decoder, handler);
	}
	@Override
	public <P extends NetworkPayload> void serverToClient(
		String channel,
		Class<P> type,
		Function<FriendlyByteBuf, P> decoder,
		PayloadHandler<P> handler
	) {
		register(channel, type, decoder, handler);
	}
	private static <P extends NetworkPayload> void register(
		String name,
		Class<P> type,
		Function<FriendlyByteBuf, P> decoder,
		PayloadHandler<P> handler
	) {
		channel(name).registerMessage(
			0, type, NetworkPayload::encode, decoder, (payload, supplier) -> {
				var context = supplier.get();
				context.enqueueWork(() -> handler.handle(payload, new ForgeContext(context)));
				context.setPacketHandled(true);
			}
		);
	}
	private record ForgeContext(NetworkEvent.Context context) implements PayloadContext {
		@Override
		public @Nullable ServerPlayer player() {
			return context.getSender();
		}
		@Override
		public void enqueue(Runnable task) {
			context.enqueueWork(task);
		}
	}
}
