package io.github.forgestove.flexconfig.client;
import com.terraformersmc.modmenu.api.*;
import io.github.forgestove.flexconfig.*;
import net.fabricmc.api.*;
/**
 * ModMenu 入口：把本模组的配置界面挂到模组列表。
 * <p>
 * 返回值写成全限定名是因为与本项目的 {@link ConfigScreenFactory} 同名。
 */
public class FlexConfigModMenu implements ModMenuApi {
	@Override
	@Environment(EnvType.CLIENT)
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ConfigRegistry.getHandlers(FlexConfig.ID).isEmpty()
			? null
			: parent -> FlexConfigScreenFactory.createConfigScreen(FlexConfig.ID);
	}
}
