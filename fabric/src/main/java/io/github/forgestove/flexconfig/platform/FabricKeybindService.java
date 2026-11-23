package io.github.forgestove.flexconfig.platform;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.flexconfig.platform.services.KeybindService;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
public class FabricKeybindService implements KeybindService {
	@Override
	public Key boundKey(KeyMapping mapping) {
		return KeyBindingHelper.getBoundKeyOf(mapping);
	}
}
