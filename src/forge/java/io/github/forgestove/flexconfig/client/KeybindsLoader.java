package io.github.forgestove.flexconfig.client;
import com.mojang.blaze3d.platform.InputConstants.Key;
import net.minecraft.client.KeyMapping;
/** Forge 侧实现：Forge 给 {@code KeyMapping} 补了公开的 {@code getKey()}。 */
class KeybindsLoader {
	static Key boundKey(KeyMapping mapping) {
		return mapping.getKey();
	}
}
