package io.github.forgestove.flexconfig.demo;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.flexconfig.FlexConfig;
import io.github.forgestove.flexconfig.api.*;

import java.awt.Point;
import java.util.*;
/**
 * 开发期 UI 演示配置：覆盖所有 entry 类型，供 {@code runClient} 直接查看界面效果。
 * <p>
 * 整个 {@code demo} 包已在构建脚本里排除，不会进入发布产物；移植到别处时删掉这个包即可。
 * <p>
 * 字段数量刻意偏多：每个分类都要超出一屏，滚动条才有得测。
 */
@Config(FlexConfig.ID)
@SuppressWarnings("unused") // 配置字段全部由反射读取
public final class DemoConfig {
	@Category public Toggles toggles = new Toggles();
	@Category public Numbers numbers = new Numbers();
	public enum Theme {
		Default,
		Dark,
		Light
	}
	/** 布尔开关：本插件新写的 Ore UI 风格 OreSwitchButton。 */
	public static class Toggles {
		public boolean enabled = true;
		public boolean fancyMode = false;
		@RequiresRestart public boolean heavyFeature = false;
		@Category(false) public Nested nested = new Nested();
		/** 杂项：排序、条件、作弊标记等元数据，嵌在第一个分类底下当子分类。 */
		@Category(false) public Extras extras = new Extras();
		// —— 以下纯粹凑数量，凑够一屏好测滚动条 ——
		public boolean fadeIn = true;
		public boolean rippleEffect = false;
		public boolean compactRows = false;
		public boolean showTooltips = true;
		public boolean animateTabs = true;
		public boolean coloredIcons = false;
		public boolean stickyHeaders = true;
		public boolean keyboardNav = true;
		public boolean soundFeedback = true;
		public boolean highlightChanges = true;
		public boolean showDefaults = false;
		public boolean groupByType = false;
		public boolean searchBar = false;
		public boolean wideMode = false;
		public boolean rememberScroll = true;
		public boolean confirmDiscard = true;
		public boolean autoSave = false;
		public boolean livePreview = true;
		public boolean debugOverlay = false;
		public boolean verboseLogging = false;
		@Category public Choices choices = new Choices();
		public static class Nested {
			public boolean deep = true;
		}
		public static class Extras {
			@Order(-1) public boolean appearsFirst = true;
			@Order(1) @RequiresRestart public boolean appearsLast = false;
			@WarnCheat public boolean cheaty = false;
			@Condition(value = "minecraft") public boolean visibleWithMod = true;
			@Condition(value = "definitely_not_installed") public boolean hiddenWithoutMod = true;
		}
	}
	/** 数值：整数/长整/浮点/双精度，含范围限制与颜色值（{@code Integer} + {@link ColorValue}）。 */
	public static class Numbers {
		@IntRange(min = 0, max = 100, slider = true) public int percent = 50;
		@LongRange(min = 0) public long seed = 123456789L;
		@FloatRange(min = 0f, max = 1f, slider = true) public float scale = 0.5f;
		@DoubleRange(min = 0.0, max = 10.0, slider = true) public double multiplier = 1.5;
		@ColorValue public int accentColor = 0x3C8527;
		@ColorValue(true) public int overlayColor = 0x801E1E1F;
		@IntRange(min = -50, max = 50, slider = true) public int offsetX = 0;
		@IntRange(min = -50, max = 50, slider = true) public int offsetY = 0;
		@IntRange(min = 1, max = 5, slider = true) public int precision = 2;
		@LongRange(min = 0) public long cacheSize = 4096L;
		@FloatRange(min = 0f, max = 4f) public float animationSpeed = 1f;
		@FloatRange(min = 0f, max = 360f) public float hueShift = 0f;
		@DoubleRange(min = 0.0, max = 100.0) public double smoothing = 25.0;
	}
	/** 文本/枚举/列表/坐标/按键。 */
	public static class Choices {
		@StringLength(min = 1, max = 32) public String name = "flexconfig";
		public Theme theme = Theme.Dark;
		public List<String> keywords = new ArrayList<>(List.of("ore", "ui", "demo"));
		public List<Theme> themeCycle = new ArrayList<>(List.of(Theme.Dark, Theme.Light));
		public Point offset = new Point(0, 0);
		public Key toggleKey = InputConstants.UNKNOWN;
		@StringLength(max = 64) public String description = "Ore UI demo";
		public Theme fallbackTheme = Theme.Default;
		public Theme hoverTheme = Theme.Light;
		public List<String> aliases = new ArrayList<>(List.of("fc", "flex"));
		public Point anchor = new Point(4, 4);
		public Key resetKey = InputConstants.UNKNOWN;
	}
}
