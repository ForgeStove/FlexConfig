package io.github.forgestove.flexconfig.client;
import com.mojang.blaze3d.platform.InputConstants.Key;
import net.minecraft.client.KeyMapping;
/** NeoForge 侧实现：直接用注入到 {@code KeyMapping} 上的 {@code getKey()}。 */
final class KeybindsLoader {
	static Key boundKey(KeyMapping mapping) {
		return mapping.getKey();
	}
}
