package demo;
import io.github.forgestove.flexconfig.*;
import io.github.forgestove.flexconfig.client.FlexConfigScreenFactory;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screens.TitleScreen;
public final class FabricDemoEntry implements ClientModInitializer {
	/** 改为 false 则不在标题界面自动打开（仍可从模组列表进入）。 */
	private static final boolean AUTO_OPEN = true;
	private static boolean opened;
	@Override
	public void onInitializeClient() {
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
