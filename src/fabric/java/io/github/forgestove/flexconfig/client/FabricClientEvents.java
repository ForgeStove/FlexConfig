package io.github.forgestove.flexconfig.client;
import io.github.forgestove.flexconfig.platform.FabricPayloads;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.*;
/**
 * Fabric 的客户端事件订阅适配层，对应 NeoForge 的 {@code ConfigEvents.Client}。
 * <p>
 * S2C 接收器只能在这里注册（{@code ClientPlayNetworking} 是客户端专属类）。
 */
public final class FabricClientEvents implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FabricPayloads.registerClientReceivers();
		ClientTickEvents.END_CLIENT_TICK.register(client -> ClientLifecycleHandler.tick());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientLifecycleHandler.onLoggingOut());
		// Fabric 的鼠标事件是「每个 Screen 各注册一次」，对应 NeoForge 的全局 ScreenEvent.MouseButtonPressed.Pre
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) ->
			ScreenMouseEvents.beforeMouseClick(screen)
				.register((s, mouseX, mouseY, button) -> ClientLifecycleHandler.onScreenMousePressed(mouseX, mouseY)));
	}
}
