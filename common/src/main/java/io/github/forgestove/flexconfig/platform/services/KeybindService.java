package io.github.forgestove.flexconfig.platform.services;
import com.mojang.blaze3d.platform.InputConstants.Key;
import net.minecraft.client.KeyMapping;
public interface KeybindService {
	Key boundKey(KeyMapping mapping);
}
