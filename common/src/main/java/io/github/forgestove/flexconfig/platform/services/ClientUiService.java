package io.github.forgestove.flexconfig.platform.services;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Supplier;
/** 把配置界面挂到「模组列表」的入口；只在客户端加载。 */
public interface ClientUiService {
	void registerConfigScreen(String modId, Supplier<Screen> screenFactory);
}
