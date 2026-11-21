package io.github.forgestove.flexconfig.foundation.client;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.flexconfig.foundation.ui.core.UiTexts;
import io.github.forgestove.flexconfig.foundation.ui.factory.Keybinds;
import io.github.forgestove.flexconfig.platform.*;
import net.minecraft.client.*;
import net.minecraft.network.chat.Component;
public class ClientUtil {
	static {
		UiTexts.install(MinecraftTextEngine.INSTANCE);
		Keybinds.install(MinecraftKeybinds.INSTANCE);
	}
	public static final Minecraft mc = Minecraft.getInstance();
	public static Component modName(String modId) {
		var name = FlexConfigServices.PLATFORM.modDisplayName(modId);
		return Component.literal(name != null ? name : modId);
	}
	/**
	 * {@code KeyMapping} 在 vanilla 中没有公开的取值途径（字段为 private），NeoForge 注入了 {@code getKey()}、
	 * Fabric 要借助 Fabric API，所以差异由 {@link io.github.forgestove.flexconfig.platform.services.KeybindService} 提供。
	 */
	public static Key boundKey(KeyMapping mapping) {
		return FlexConfigClientServices.KEYBINDS.boundKey(mapping);
	}
}
