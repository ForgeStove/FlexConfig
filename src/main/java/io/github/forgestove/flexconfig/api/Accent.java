package io.github.forgestove.flexconfig.api;
import java.awt.Color;
public class Accent {
	public static final Accent DEFAULT = of(0xFF3C8527);
	private final int base, lightest, light, bright, strong, darkest, ink, wash;
	private Accent(int base, int lightest, int light, int bright, int strong, int darkest, int ink, int wash) {
		this.base = base;
		this.lightest = lightest;
		this.light = light;
		this.bright = bright;
		this.strong = strong;
		this.darkest = darkest;
		this.ink = ink;
		this.wash = wash;
	}
	public static Accent of(int base) {
		base |= 0xFF000000;
		var red = base >> 16 & 0xFF;
		var green = base >> 8 & 0xFF;
		var blue = base & 0xFF;
		var hsb = Color.RGBtoHSB(red, green, blue, null);
		var h = hsb[0];
		var s = hsb[1];
		var b = hsb[2];
		var luminance = (0.299 * red + 0.587 * green + 0.114 * blue) / 255D;
		var ink = luminance < 0.5 ? 0xFFFFFFFF : hsb(h, s * 0.92F, b * 0.11F);
		return new Accent(
			base,
			hsb(h, s * 0.819F, b * 1.09F),
			hsb(h, s * 0.676F, b * 1.18F),
			hsb(h, s * 0.96F, b * 1.24F),
			hsb(h, s * 1.018F, b * 0.75F),
			hsb(h, s * 1.065F, b * 0.48F),
			ink,
			base & 0xFFFFFF | 0x2E000000
		);
	}
	private static int hsb(float h, float s, float b) {
		return 0xFF000000 | Color.HSBtoRGB(h, Math.min(1F, s), Math.min(1F, b)) & 0xFFFFFF;
	}
	public int base() {
		return base;
	}
	public int lightest() {
		return lightest;
	}
	public int light() {
		return light;
	}
	public int bright() {
		return bright;
	}
	public int strong() {
		return strong;
	}
	public int darkest() {
		return darkest;
	}
	public int ink() {
		return ink;
	}
	public int wash() {
		return wash;
	}
}
