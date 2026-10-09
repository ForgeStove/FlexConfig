package io.github.forgestove.flexconfig.client;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
public class ClientUtil {
	public static final Minecraft mc = Minecraft.getInstance();
	/**
	 * 取按键当前绑定的键。{@code KeyMapping} 在 vanilla 里没有公开 getter（字段是 private）——
	 * NeoForge 注入了 {@code getKey()}，Fabric 要用 Fabric API，故差异下沉到各自源集的 {@code KeybindsLoader}。
	 */
	public static InputConstants.Key boundKey(KeyMapping mapping) {
		return KeybindsLoader.boundKey(mapping);
	}
}
