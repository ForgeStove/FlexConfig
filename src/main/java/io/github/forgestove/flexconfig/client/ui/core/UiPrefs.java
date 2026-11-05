package io.github.forgestove.flexconfig.client.ui.core;
import io.github.forgestove.flexconfig.client.ClientUtil;
import net.minecraft.network.chat.Component;

import java.nio.file.*;
/** 菜单尺寸与高对比度偏好，保存在 config/flexconfig/ui_prefs.txt。 */
public class UiPrefs {
	public enum Size {
		SMALLER,
		AUTO,
		LARGER
	}
	private static Size size = Size.AUTO;
	private static boolean highContrast;
	static {
		load();
	}
	private static Path file() {
		return ClientUtil.mc.gameDirectory.toPath().resolve("config").resolve("flexconfig").resolve("ui_prefs.txt");
	}
	private static void load() {
		try {
			var path = file();
			if (!Files.isRegularFile(path)) return;
			for (var line : Files.readAllLines(path)) {
				var parts = line.split("=", 2);
				if (parts.length != 2) continue;
				if ("size".equals(parts[0])) {
					for (var value : Size.values()) if (value.name().equalsIgnoreCase(parts[1].trim())) size = value;
				} else if ("contrast".equals(parts[0])) highContrast = Boolean.parseBoolean(parts[1].trim());
			}
		} catch (Exception ignored) {}
	}
	private static void save() {
		try {
			var path = file();
			Files.createDirectories(path.getParent());
			Files.writeString(path, "size=" + size.name() + "\ncontrast=" + highContrast + "\n");
		} catch (Exception ignored) {}
	}
	public static boolean highContrast() {
		return highContrast;
	}
	public static void toggleHighContrast() {
		highContrast = !highContrast;
		save();
	}
	/** 高对比模式把灰阶压到黑白，保留金色/红色语义色。 */
	public static int highContrastColor(int argb) {
		if (!highContrast) return argb;
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
	public static Size size() {
		return size;
	}
	public static void cycleSize() {
		size = switch (size) {
			case SMALLER -> Size.AUTO;
			case AUTO -> Size.LARGER;
			case LARGER -> Size.SMALLER;
		};
		save();
	}
	public static int zoom(int guiWidth, int guiHeight) {
		var z = UiMath.clamp((int) Math.floor(Math.min(guiWidth / 480D, guiHeight / 270D)), 1, 3);
		if (size == Size.SMALLER) return Math.max(1, z - 1);
		if (size == Size.LARGER && z < 3 && guiWidth / (z + 1) >= 320 && guiHeight / (z + 1) >= 240) return z + 1;
		return z;
	}
	public static Component label() {
		return switch (size) {
			case SMALLER -> Component.translatableWithFallback("flexconfig.menu.size.smaller", "Smaller");
			case AUTO -> Component.translatableWithFallback("flexconfig.menu.size.auto", "Auto");
			case LARGER -> Component.translatableWithFallback("flexconfig.menu.size.larger", "Larger");
		};
	}
}
