package io.github.forgestove.flexconfig.platform;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.flexconfig.platform.services.KeybindService;
import net.minecraft.client.KeyMapping;
public class NeoForgeKeybindService implements KeybindService {
	@Override
	public Key boundKey(KeyMapping mapping) {
		return mapping.getKey();
	}
}
