package io.github.forgestove.flexconfig.client;
import io.github.forgestove.flexconfig.*;
import io.github.forgestove.flexconfig.api.ConfigType;
import io.github.forgestove.flexconfig.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.*;
/** 配置界面的构建入口；挂到模组列表的动作由加载器实现（{@code Platform#registerConfigScreen}）负责。 */
public final class FlexConfigScreenFactory {
	/**
	 * 该模组的配置入口。
	 * 只有一份配置、且此刻可用时直接打开它；多份、或唯一那份不可用时先进 {@link ConfigFileScreen}（那边会把不可用的置灰）。
	 */
	@Contract("_ -> new")
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
