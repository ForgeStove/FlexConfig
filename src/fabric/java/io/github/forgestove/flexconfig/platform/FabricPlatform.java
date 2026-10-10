package io.github.forgestove.flexconfig.platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.Supplier;
/** {@link Platform} 的 Fabric 实现。 */
public final class FabricPlatform implements Platform {
	/** 当前服务端实例；由 {@code FabricEvents} 在启动/停止时维护，仅用于广播（Fabric 没有全局广播 API）。 */
	public static MinecraftServer server;
	@Override
	public Path configDir() {
		return FabricLoader.getInstance().getConfigDir();
	}
	@Override
	public boolean isModLoaded(String modId) {
		return FabricLoader.getInstance().isModLoaded(modId);
	}
	@Override
	public boolean isClient() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
	}
	@Override
	public boolean isDevelopmentEnvironment() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}
	@Override
	public @Nullable String modDisplayName(String modId) {
		return FabricLoader.getInstance().getModContainer(modId).map(container -> container.getMetadata().getName()).orElse(null);
	}
	/**
	 * Fabric 上无法替其他模组挂配置界面：ModMenu 的 {@code ModMenuApi} 只允许每个模组为自己声明入口。
	 * <p>
	 * 使用本库的模组需要自己实现 {@code ModMenuApi}（返回 {@code ConfigScreenFactory.createConfigScreen(自己的 modId)}），
	 * 或提供自己的按键/命令入口。NeoForge 侧靠 {@code IConfigScreenFactory} 扩展点可以代注册，故那边有实现。
	 */
	@Override
	public void registerConfigScreen(String modId, Supplier<Screen> screenFactory) {}
	@Override
	public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
		ServerPlayNetworking.send(player, payload);
	}
	@Override
	public void sendToAllPlayers(CustomPacketPayload payload) {
		var current = server;
		if (current == null) return;
		for (var player : current.getPlayerList().getPlayers()) ServerPlayNetworking.send(player, payload);
	}
	/** 客户端专属 API，写在全限定名里靠 JVM 惰性解析隔离——专用服务器不会执行到这里。 */
	@Override
	public void sendToServer(CustomPacketPayload payload) {
		ClientPlayNetworking.send(payload);
	}
	@Override
	public boolean canSendToPlayer(ServerPlayer player, Type<?> type) {
		return ServerPlayNetworking.canSend(player, type);
	}
}
