package io.github.forgestove.flexconfig.foundation.ui.core;
import net.minecraft.util.Mth;
/** 界面配色和尺寸常量。 */
public class UiTheme {
	public static int fadeToward(int argb, int surface, float amount) {
		var t = Mth.clamp(amount, 0F, 1F);
		var r = Math.round((surface >> 16 & 0xFF) + ((argb >> 16 & 0xFF) - (surface >> 16 & 0xFF)) * t);
		var g = Math.round((surface >> 8 & 0xFF) + ((argb >> 8 & 0xFF) - (surface >> 8 & 0xFF)) * t);
		var b = Math.round((surface & 0xFF) + ((argb & 0xFF) - (surface & 0xFF)) * t);
		return argb & 0xFF000000 | r << 16 | g << 8 | b;
	}
	public static int mulAlpha(int argb, float factor) {
		var a = Math.round((argb >>> 24 & 0xFF) * Mth.clamp(factor, 0F, 1F));
		return a << 24 | argb & 0xFFFFFF;
	}
	public static final int BACKDROP_TOP = 0x8C000000;
	public static final int SHADOW = 0x59000000;
	public static final int WINDOW_EDGE = 0xFF1E1E1F;
	public static final int TITLEBAR = 0xFFE4E6E6;
	public static final int TITLEBAR_EDGE = 0xFFB1B2B5;
	public static final int TITLE_INK = 0xFF1E1E1F;
	public static final int ICON_INK = 0xFF5A5C60;
	public static final int PANEL = 0xFF48494A;
	public static final int SIDEBAR = 0xFF313233;
	public static final int BORDER = 0xFF1E1E1F;
	public static final int BORDER_LIGHT = 0xFFA8A8AE;
	public static final int SEP = 0xFF323334;
	/** 比 {@link #SIDEBAR} 深一档；{@link #SEP} 与侧边栏背景几乎同色，选中项底边要用这个。 */
	public static final int SEP_DARK = 0xFF28292A;
	public static final int GROOVE_LIGHT = 0xFF5A5B5C;
	public static final int WELL = 0xFF1F2021;
	public static final int OUTLINE = 0xFFA8A8AE;
	public static final int TEXT = 0xFFFFFFFF;
	public static final int TEXT_2 = 0xFFD4D4D4;
	public static final int TEXT_3 = 0xFFBDBDBD;
	public static final int MUTED = 0xFFD4D4D4;
	public static final int DISABLED = 0xFF7E7F80;
	public static final int DANGER = 0xFFC23B30;
	public static final int DANGER_LIGHT = 0xFFD0453A;
	public static final int DANGER_DEPTH = 0xFF5E1A14;
	public static final int LOCKED = 0xFF7E7F80;
	public static final int ROW_HOVER = 0xFF505152;
	public static final int ROW_PRESS = 0xFF3C3D3E;
	public static final int FIELD = 0xFF262728;
	public static final int FIELD_SHADOW = 0xFF161718;
	public static final int TRACK_LIGHT = 0xFF3A3B3C;
	public static final int SECONDARY_FACE = 0xFFD0D1D4;
	public static final int SECONDARY_TOP = 0xFFE3E4E6;
	public static final int SECONDARY_SHINE = 0xFFF7F7F9;
	public static final int SECONDARY_LIP = 0xFF58585A;
	public static final int SECONDARY_HOVER = 0xFFDFE0E2;
	public static final int SECONDARY_PRESSED = 0xFFBDBEC1;
	public static final int ROW_HEIGHT = 34;
	public static final int ROW_GAP = 0;
	public static final int SECTION_HEIGHT = 22;
	public static final int GROUP_HEIGHT = 22;
	public static final int CONTROL_HEIGHT = 16;
	public static final int CONTROL_WIDTH = 110;
	public static final int ACTION_SIZE = 16;
	public static final int GAP = 4;
	public static final int INDENT = 8;
	public static final int PADDING = 6;
	public static final int HEADER_HEIGHT = 22;
	public static final int SCROLLBAR_WIDTH = 8;
	public static final int MIN_PANEL_WIDTH = 400;
	public static final int MIN_PANEL_HEIGHT = 240;
	public static final int MAX_PANEL_WIDTH = 640;
	public static final int MAX_PANEL_HEIGHT = 384;
	public static final int TAB_PITCH = 20;
	public static final int PAGE_HEADER = 24;
	public static final int LEFT_MIN = 120;
	public static final int LEFT_MAX = 176;
}
