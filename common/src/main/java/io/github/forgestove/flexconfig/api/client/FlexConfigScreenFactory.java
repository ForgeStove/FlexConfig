package io.github.forgestove.flexconfig.api.client;
import io.github.forgestove.flexconfig.api.ConfigType;
import io.github.forgestove.flexconfig.api.registration.*;
import io.github.forgestove.flexconfig.foundation.client.ClientUtil;
import io.github.forgestove.flexconfig.foundation.ui.*;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
public class FlexConfigScreenFactory {
	/**
	 * 只有一份配置并且可用时直接打开，否则进入 {@link ConfigFileScreen}；不可用的项在列表里显示为灰色。
	 */
	public static @NotNull Screen createConfigScreen(String modId) {
		var handlers = ConfigRegistry.getHandlers(modId);
		if (handlers.size() == 1 && available(handlers.get(0))) return new ConfigScreen<>(handlers.get(0));
		return new ConfigFileScreen(modId, handlers);
	}
	/** SERVER 型按世界单独存储：不在世界里时既无处可读也无处可写，因此不可进入。 */
	public static boolean available(@NotNull ConfigHandler<?, ?> handler) {
		return handler.getType() != ConfigType.SERVER || ClientUtil.mc.level != null;
	}
}
