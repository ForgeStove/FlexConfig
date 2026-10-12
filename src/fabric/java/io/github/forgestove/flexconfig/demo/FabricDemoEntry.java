package io.github.forgestove.flexconfig.demo;
import io.github.forgestove.flexconfig.*;
import io.github.forgestove.flexconfig.client.FlexConfigScreenFactory;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screens.TitleScreen;
/**
 * Fabric 的开发期演示入口，对应 NeoForge 的 {@code NeoForgeDemoEntry}。
 * <p>
 * 整个 {@code demo} 包不进发布产物，故由 {@code FabricClientEvents} 只在开发环境调用。
 */
public final class FabricDemoEntry {
	/** 改为 false 则不在标题界面自动打开（仍可从模组列表进入）。 */
	private static final boolean AUTO_OPEN = true;
	private static boolean opened;
	public static void register() {
		ConfigRegistry.init(DemoConfig.class);
		ConfigRegistry.init(DemoServerConfig.class);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (opened || !AUTO_OPEN) return;
			if (!(client.screen instanceof TitleScreen)) return;
			opened = true;
			client.setScreen(FlexConfigScreenFactory.createConfigScreen(FlexConfig.ID));
		});
	}
}
