package demo;
import io.github.forgestove.flexconfig.*;
import io.github.forgestove.flexconfig.client.FlexConfigScreenFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
@EventBusSubscriber(modid = DemoMod.ID, value = Dist.CLIENT)
public final class NeoForgeDemoEntry {
	/** 改为 false 则不在标题界面自动打开（仍可从模组列表进入）。 */
	private static final boolean AUTO_OPEN = true;
	private static boolean opened;
	@SubscribeEvent
	public static void onClientTick(Post event) {
		if (opened || !AUTO_OPEN) return;
		var mc = Minecraft.getInstance();
		if (!(mc.screen instanceof TitleScreen)) return;
		opened = true;
		mc.setScreen(FlexConfigScreenFactory.createConfigScreen(FlexConfig.ID));
	}
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		ConfigRegistry.init(DemoConfig.class);
		ConfigRegistry.init(DemoServerConfig.class);
	}
}
