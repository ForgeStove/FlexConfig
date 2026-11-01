package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.FlexConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.loading.*;
import net.minecraftforge.network.NetworkEvent.Context;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.*;
/** {@link Platform} 的 Forge 1.20.1 实现。 */
public final class ForgePlatform implements Platform {
	private static SimpleChannel channel;
	private static int nextId;
	/** 由 {@link io.github.forgestove.flexconfig.ForgeEvents} 在 common setup 里调用，把 SimpleChannel 交给核心。 */
	public static void registerPayloads(Consumer<Payloads> consumer) {
		channel = NetworkRegistry.newSimpleChannel(
			new ResourceLocation(FlexConfig.ID, "main"),
			() -> FlexConfig.NETWORK_VERSION,
			FlexConfig.NETWORK_VERSION::equals,
			FlexConfig.NETWORK_VERSION::equals
		);
		consumer.accept(new ForgePayloads(channel));
	}
	@Override
	public Path configDir() {
		return FMLPaths.CONFIGDIR.get();
	}
	@Override
	public boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
	}
	@Override
	public boolean isClient() {
		return FMLEnvironment.dist.isClient();
	}
	@Override
	public boolean isDevelopmentEnvironment() {
		return !FMLLoader.isProduction();
	}
	@Override
	public @Nullable String modDisplayName(String modId) {
		return ModList.get().getModContainerById(modId).map(container -> container.getModInfo().getDisplayName()).orElse(null);
	}
	/**
	 * 1.20.1 的扩展点只能挂在当前模组自身，无法按 modId 代注册；
	 * 因此只给 FlexConfig 自己挂，使用本库的模组需要自行提供入口。
	 */
	@Override
	public void registerConfigScreen(String modId, Supplier<Screen> screenFactory) {
		if (!modId.equals(FlexConfig.ID)) return;
		ModLoadingContext.get()
			.registerExtensionPoint(ConfigScreenFactory.class, () -> new ConfigScreenFactory((client, parent) -> screenFactory.get()));
	}
	@Override
	public void sendToPlayer(ServerPlayer player, NetworkPayload payload) {
		channel.send(PacketDistributor.PLAYER.with(() -> player), payload);
	}
	@Override
	public void sendToAllPlayers(NetworkPayload payload) {
		channel.send(PacketDistributor.ALL.noArg(), payload);
	}
	@Override
	public void sendToServer(NetworkPayload payload) {
		channel.sendToServer(payload);
	}
	@Override
	public boolean canSendToPlayer(ServerPlayer player, String channel) {
		return ForgePlatform.channel.isRemotePresent(player.connection.connection);
	}
	private record ForgePayloads(SimpleChannel channel) implements Payloads {
		@Override
		public <P extends NetworkPayload> void clientToServer(
			String name,
			Class<P> type,
			Function<FriendlyByteBuf, P> decoder,
			PayloadHandler<P> handler
		) {
			register(type, decoder, handler);
		}
		@Override
		public <P extends NetworkPayload> void serverToClient(
			String name,
			Class<P> type,
			Function<FriendlyByteBuf, P> decoder,
			PayloadHandler<P> handler
		) {
			register(type, decoder, handler);
		}
		private <P extends NetworkPayload> void register(Class<P> type, Function<FriendlyByteBuf, P> decoder, PayloadHandler<P> handler) {
			ForgePlatform.channel.registerMessage(
				nextId++, type, NetworkPayload::encode, decoder, (msg, supplier) -> {
					var context = supplier.get();
					context.enqueueWork(() -> handler.handle(msg, new ForgeContext(context)));
					context.setPacketHandled(true);
				}
			);
		}
	}
	private record ForgeContext(Context context) implements PayloadContext {
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
