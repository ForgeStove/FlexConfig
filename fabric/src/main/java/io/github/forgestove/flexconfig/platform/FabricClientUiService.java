package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.platform.services.ClientUiService;
import net.minecraft.client.gui.screens.Screen;

import java.util.function.Supplier;
/**
 * Fabric 上无法替其他模组挂配置界面：ModMenu 的 {@code ModMenuApi} 只允许每个模组为自己声明入口。
 * <p>
 * 使用本库的模组需要自己实现 {@code ModMenuApi}（返回 {@code FlexConfigScreenFactory.createConfigScreen(自己的 modId)}），
 * 或提供自己的按键/命令入口。NeoForge 侧靠 {@code IConfigScreenFactory} 扩展点可以代注册，故那边有实现。
 */
public class FabricClientUiService implements ClientUiService {
	@Override
	public void registerConfigScreen(String modId, Supplier<Screen> screenFactory) {}
}
