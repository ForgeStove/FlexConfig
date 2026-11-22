package io.github.forgestove.flexconfig.foundation.client;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.flexconfig.foundation.ui.core.UiTexts;
import io.github.forgestove.flexconfig.foundation.ui.factory.Keybinds;
import io.github.forgestove.flexconfig.platform.*;
import net.minecraft.client.*;
import net.minecraft.network.chat.Component;
public class ClientUtil {
	public static final Minecraft mc = Minecraft.getInstance();
	static {
		UiTexts.install(MinecraftTextEngine.INSTANCE);
		Keybinds.install(MinecraftKeybinds.INSTANCE);
	}
	public static Component modName(String modId) {
		var name = FlexConfigServices.PLATFORM.modDisplayName(modId);
		return Component.literal(name != null ? name : modId);
	}
	public static Key boundKey(KeyMapping mapping) {
		return FlexConfigClientServices.KEYBINDS.boundKey(mapping);
	}
}
