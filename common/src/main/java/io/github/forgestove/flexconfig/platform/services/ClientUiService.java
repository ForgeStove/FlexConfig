package io.github.forgestove.flexconfig.platform.services;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Supplier;
public interface ClientUiService {
	void registerConfigScreen(String modId, Supplier<Screen> screenFactory);
}
