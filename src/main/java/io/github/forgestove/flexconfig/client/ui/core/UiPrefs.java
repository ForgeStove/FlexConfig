package io.github.forgestove.flexconfig.client.ui.core;
import io.github.forgestove.flexconfig.client.ClientUtil;
public class UiPrefs {
	public static int highContrastColor(int argb) {
		if (!ClientUtil.mc.options.highContrast().get()) return argb;
		var r = argb >> 16 & 0xFF;
		var g = argb >> 8 & 0xFF;
		var b = argb & 0xFF;
		var max = Math.max(r, Math.max(g, b));
		var min = Math.min(r, Math.min(g, b));
		int rgb;
		if (max - min < 30) rgb = max >= 128 ? 0xFFFFFF : 0x000000;
		else if (r >= g && g >= b) rgb = 0xFFFFD866;
		else if (r >= g && r >= b) rgb = 0xFFFF6B6B;
		else rgb = 0xFFFFFFFF;
		return argb & 0xFF000000 | rgb;
	}
	public static int zoom(int guiWidth, int guiHeight) {
		return UiMath.clamp((int) Math.floor(Math.min(guiWidth / 480D, guiHeight / 270D)), 1, 3);
	}
}
