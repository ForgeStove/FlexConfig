package io.github.forgestove.flexconfig.platform;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Supplier;
/** 加载器需要提供的少量能力：核心只依赖本接口与 Minecraft 自身，实现放在 {@code platform/<loader>} 下，事件订阅留在各自的适配类里。 */
public interface Platform {
	static Platform get() {
		return Holder.INSTANCE;
	}
	Path configDir();
	boolean isModLoaded(String modId);
	boolean isClient();
	/** 只用来跑开发期检查，例如缺失翻译键告警。 */
	boolean isDevelopmentEnvironment();
	/** 该模组未加载时返回 {@code null}。 */
	@Nullable String modDisplayName(String modId);
	/** 把配置界面挂到「模组列表」的入口（NeoForge 用 {@code IConfigScreenFactory}，Fabric 用 ModMenu）。 */
	void registerConfigScreen(String modId, Supplier<Screen> screenFactory);
	void sendToPlayer(ServerPlayer player, NetworkPayload payload);
	void sendToAllPlayers(NetworkPayload payload);
	void sendToServer(NetworkPayload payload);
	/** 对方客户端没装本模组时不能发，否则加载器会抛异常。 */
	boolean canSendToPlayer(ServerPlayer player, String channel);
	/** 包处理器上下文的最小面。 */
	interface PayloadContext {
		@Nullable ServerPlayer player();
		void enqueue(Runnable task);
	}
	@FunctionalInterface
	interface PayloadHandler<P extends NetworkPayload> {
		void handle(P payload, PayloadContext context);
	}
	interface Payloads {
		<P extends NetworkPayload> void clientToServer(String channel, Class<P> type, Function<FriendlyByteBuf, P> decoder,
			PayloadHandler<P> handler);
		<P extends NetworkPayload> void serverToClient(String channel, Class<P> type, Function<FriendlyByteBuf, P> decoder,
			PayloadHandler<P> handler);
	}
	class Holder {
		private static final Platform INSTANCE = PlatformLoader.load();
	}
}
