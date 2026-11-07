package io.github.forgestove.flexconfig.client;
import com.terraformersmc.modmenu.api.*;
import io.github.forgestove.flexconfig.FlexConfig;
import io.github.forgestove.flexconfig.api.*;
import net.fabricmc.api.*;
public class FlexConfigModMenu implements ModMenuApi {
	@Override
	@Environment(EnvType.CLIENT)
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		if (ConfigRegistry.getHandlers(FlexConfig.ID).isEmpty()) return null;
		return parent -> FlexConfigScreenFactory.createConfigScreen(FlexConfig.ID);
	}
}
