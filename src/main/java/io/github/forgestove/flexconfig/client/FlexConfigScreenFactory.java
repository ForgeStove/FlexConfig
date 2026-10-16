package io.github.forgestove.flexconfig.client;
import io.github.forgestove.flexconfig.*;
import io.github.forgestove.flexconfig.api.ConfigType;
import io.github.forgestove.flexconfig.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
/** 配置界面的构建入口；挂到模组列表的动作由加载器侧的 {@code Platform#registerConfigScreen} 实现。 */
public final class FlexConfigScreenFactory {
	/**
	 * 仅有一份配置且此刻可用时直接打开，否则先进 {@link ConfigFileScreen}（其中不可用项置灰）。
	 */
	public static @NotNull Screen createConfigScreen(String modId) {
		var handlers = ConfigRegistry.getHandlers(modId);
		if (handlers.size() == 1 && available(handlers.getFirst())) return new ConfigScreen<>(handlers.get(0));
		return new ConfigFileScreen(modId, handlers);
	}
	/** SERVER 型按世界单独存储：不在世界里时既无处可读也无处可写，因此不可进入。 */
	public static boolean available(@NotNull ConfigHandler<?, ?> handler) {
		return handler.getType() != ConfigType.SERVER || ClientUtil.mc.level != null;
	}
}
