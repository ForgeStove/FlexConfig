package io.github.forgestove.flexconfig.client;
import com.mojang.blaze3d.platform.InputConstants.Key;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
/** Fabric 侧实现：vanilla 没有公开的 getter，用 Fabric API 的 {@code KeyBindingHelper}。 */
final class KeybindsLoader {
	static Key boundKey(KeyMapping mapping) {
		return KeyBindingHelper.getBoundKeyOf(mapping);
	}
}
