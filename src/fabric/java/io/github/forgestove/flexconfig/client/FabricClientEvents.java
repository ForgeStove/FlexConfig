package io.github.forgestove.flexconfig.client;
import io.github.forgestove.flexconfig.demo.DemoRegistration;
import io.github.forgestove.flexconfig.platform.FabricPayloads;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
/**
 * Fabric 的客户端事件订阅适配层，对应 NeoForge 的 {@code ConfigEvents.Client}。
 * <p>
 * S2C 接收器只能在这里注册（{@code ClientPlayNetworking} 是客户端专属类）。
 */
public class FabricClientEvents implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		DemoRegistration.register();
		FabricPayloads.registerClientReceivers();
		ClientTickEvents.END_CLIENT_TICK.register(client -> ClientLifecycleHandler.tick());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientLifecycleHandler.onLoggingOut());
	}
}
