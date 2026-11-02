package demo;
import io.github.forgestove.flexconfig.*;
import io.github.forgestove.flexconfig.client.FlexConfigScreenFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
@EventBusSubscriber(modid = DemoMod.ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ForgeDemoEntry {
	/** 改为 false 则不在标题界面自动打开（仍可从模组列表进入）。 */
	private static final boolean AUTO_OPEN = true;
	private static boolean opened;
	@SubscribeEvent
	public static void onClientSetup(FMLClientSetupEvent event) {
		ConfigRegistry.init(DemoConfig.class);
		ConfigRegistry.init(DemoServerConfig.class);
	}
	/** 1.20.1 的 MOD 总线与游戏总线分离，自动打开要单独挂游戏总线。 */
	@EventBusSubscriber(modid = DemoMod.ID, value = Dist.CLIENT)
	public static final class AutoOpen {
		@SubscribeEvent
		public static void onClientTick(TickEvent.ClientTickEvent event) {
			if (event.phase != TickEvent.Phase.END) return;
			if (opened || !AUTO_OPEN) return;
			var mc = Minecraft.getInstance();
			if (!(mc.screen instanceof TitleScreen)) return;
			opened = true;
			mc.setScreen(FlexConfigScreenFactory.createConfigScreen(FlexConfig.ID));
		}
	}
}
