package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.platform.services.ClientUiService;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Supplier;
public class FabricClientUiService implements ClientUiService {
	@Override
	public void registerConfigScreen(String modId, Supplier<Screen> screenFactory) {}
}
