package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.platform.services.ClientUiService;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.function.Supplier;
public class NeoForgeClientUiService implements ClientUiService {
	@Override
	public void registerConfigScreen(String modId, Supplier<Screen> screenFactory) {
		ModList.get().getModContainerById(modId).ifPresent(container -> {
			Supplier<IConfigScreenFactory> extension = () -> (modContainer, screen) -> screenFactory.get();
			container.registerExtensionPoint(IConfigScreenFactory.class, extension);
		});
	}
}
