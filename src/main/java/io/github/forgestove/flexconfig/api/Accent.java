package io.github.forgestove.flexconfig.api;
import java.awt.Color;
/**
 * 界面的强调色阶：调用者在注册配置时给一个基色，明暗、悬停和按下变体由基色的 HSB 推导。
 * 默认值对应 punchy 的金色阶。
 */
public record Accent(int base, int lightest, int light, int bright, int strong, int deep, int darkest, int ink, int wash) {
	public static final Accent GOLD = new Accent(
		0xFFF2B01E,
		0xFFFFF6D6,
		0xFFFFD866,
		0xFFFFC93D,
		0xFFD99A0C,
		0xFFA87406,
		0xFF7A5207,
		0xFF1B1405,
		0x2EF2B01E
	);
	public static Accent of(int base) {
		base |= 0xFF000000;
		if (base == GOLD.base) return GOLD;
		var hsb = Color.RGBtoHSB(base >> 16 & 0xFF, base >> 8 & 0xFF, base & 0xFF, null);
		var h = hsb[0];
		var s = hsb[1];
		var b = hsb[2];
		return new Accent(
			base,
			hsb(h, s * 0.18F, 1F),
			hsb(h, s * 0.50F, 1F),
			hsb(h, s * 0.76F, 1F),
			hsb(h, Math.min(1F, s * 1.08F), b * 0.90F),
			hsb(h, s, b * 0.66F),
			hsb(h, s, b * 0.48F),
			hsb(h, s * 0.92F, b * 0.11F),
			base & 0x00FFFFFF | 0x2E000000
		);
	}
	private static int hsb(float h, float s, float b) {
		return 0xFF000000 | Color.HSBtoRGB(h, s, Math.min(1F, b)) & 0xFFFFFF;
	}
}
