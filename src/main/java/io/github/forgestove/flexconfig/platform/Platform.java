package io.github.forgestove.flexconfig.platform;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.Supplier;
/**
 * 加载器需要提供的少量能力。核心部分只依赖本接口与 Minecraft 自身；
 * 各加载器的实现放在 {@code platform/<loader>} 下，事件订阅留在各自的加载器适配类里。
 */
public interface Platform {
	static Platform get() {
		return Holder.INSTANCE;
	}
	/** 全局配置目录（如 {@code config/}）。 */
	Path configDir();
	boolean isModLoaded(String modId);
	boolean isClient();
	/** 只用来跑开发期检查，例如缺失翻译键告警。 */
	boolean isDevelopmentEnvironment();
	/** 该模组未加载时返回 {@code null}。 */
	@Nullable String modDisplayName(String modId);
	/** 把配置界面挂到「模组列表」的入口（NeoForge 用 {@code IConfigScreenFactory}，Fabric 用 ModMenu）。 */
	void registerConfigScreen(String modId, Supplier<Screen> screenFactory);
	void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);
	void sendToAllPlayers(CustomPacketPayload payload);
	void sendToServer(CustomPacketPayload payload);
	/** 对方客户端没装本模组时不能发，否则加载器会抛异常。 */
	boolean canSendToPlayer(ServerPlayer player, Type<?> type);
	/** 包处理器上下文的最小面。 */
	interface PayloadContext {
		@Nullable ServerPlayer player();
		void enqueue(Runnable task);
	}
	@FunctionalInterface
	interface PayloadHandler<P extends CustomPacketPayload> {
		void handle(P payload, PayloadContext context);
	}
	/** 包注册入口，由具体加载器实现并传给核心。 */
	interface Payloads {
		<P extends CustomPacketPayload> void clientToServer(Type<P> type, StreamCodec<FriendlyByteBuf, P> codec,
			PayloadHandler<P> handler);
		<P extends CustomPacketPayload> void serverToClient(Type<P> type, StreamCodec<FriendlyByteBuf, P> codec,
			PayloadHandler<P> handler);
	}
	final class Holder {
		private static final Platform INSTANCE = PlatformLoader.load();
	}
}
