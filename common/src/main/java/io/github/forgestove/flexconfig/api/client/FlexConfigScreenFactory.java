package io.github.forgestove.flexconfig.api.client;
import io.github.forgestove.flexconfig.api.ConfigType;
import io.github.forgestove.flexconfig.api.registration.*;
import io.github.forgestove.flexconfig.foundation.client.ClientUtil;
import io.github.forgestove.flexconfig.foundation.client.screen.*;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
public class FlexConfigScreenFactory {
	public static @NotNull Screen createConfigScreen(String modId) {
		var handlers = ConfigRegistry.getHandlers(modId);
		if (handlers.size() == 1 && available(handlers.get(0))) return new ConfigScreen<>(handlers.get(0));
		return new ConfigFileScreen(modId, handlers);
	}
	public static boolean available(@NotNull ConfigHandler<?, ?> handler) {
		return handler.getType() != ConfigType.SERVER || ClientUtil.mc.level != null;
	}
}
