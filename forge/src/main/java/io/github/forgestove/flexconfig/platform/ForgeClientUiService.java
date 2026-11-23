package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.FlexConfig;
import io.github.forgestove.flexconfig.platform.services.ClientUiService;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.fml.ModLoadingContext;

import java.util.function.Supplier;
public class ForgeClientUiService implements ClientUiService {
	@Override
	public void registerConfigScreen(String modId, Supplier<Screen> screenFactory) {
		if (!modId.equals(FlexConfig.ID)) return;
		ModLoadingContext.get()
			.registerExtensionPoint(ConfigScreenFactory.class, () -> new ConfigScreenFactory((client, parent) -> screenFactory.get()));
	}
}
