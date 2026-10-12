package io.github.forgestove.flexconfig.demo;
import io.github.forgestove.flexconfig.*;
import io.github.forgestove.flexconfig.client.FlexConfigScreenFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
/**
 * 开发期入口：注册演示配置，并在标题界面自动打开一次。
 * <p>
 * 演示配置是以 {@link FlexConfig#ID} 注册的，因此「模组列表 → Flex Config → Config」按钮同样能进入它。
 */
@EventBusSubscriber(modid = FlexConfig.ID, value = Dist.CLIENT)
public final class NeoForgeDemoEntry {
	/** 改为 false 则不在标题界面自动打开（仍可从模组列表进入）。 */
	private static final boolean AUTO_OPEN = true;
	private static boolean opened;
	/** 标题界面出现后自动打开演示界面一次，省去从模组列表点进来的步骤。 */
	@SubscribeEvent
	public static void onClientTick(Post event) {
		if (opened || !AUTO_OPEN) return;
		var mc = Minecraft.getInstance();
		if (!(mc.screen instanceof TitleScreen)) return;
		opened = true;
		mc.setScreen(FlexConfigScreenFactory.createConfigScreen(FlexConfig.ID));
	}
	/** 注册演示配置，早于任何界面创建。两份配置让模组入口先走配置文件选择界面。 */
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		ConfigRegistry.init(DemoConfig.class);
		ConfigRegistry.init(DemoServerConfig.class);
	}
}
