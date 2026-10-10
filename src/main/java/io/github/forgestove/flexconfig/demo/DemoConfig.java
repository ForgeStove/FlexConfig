package io.github.forgestove.flexconfig.demo;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.flexconfig.FlexConfig;
import io.github.forgestove.flexconfig.api.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
/**
 * 开发期 UI 演示配置：覆盖所有 entry 类型，供 {@code runClient} 直接查看界面效果。
 * <p>
 * 整个 {@code demo} 包已在构建脚本里排除，不会进入发布产物；移植到别处时删掉这个包即可。
 */
@Config(FlexConfig.ID)
@SuppressWarnings("unused") // 配置字段全部由反射读取
public final class DemoConfig {
	@Category public Toggles toggles = new Toggles();
	@Category public Numbers numbers = new Numbers();
	@Category public Choices choices = new Choices();
	@Category(false) public Extras extras = new Extras();
	/** 布尔开关：本插件新写的 Ore UI 风格 ToggleSwitch。 */
	public static class Toggles {
		public boolean enabled = true;
		public boolean fancyMode = false;
		@RequiresRestart public boolean heavyFeature = false;
		@Category(false) public Nested nested = new Nested();
		public static class Nested {
			public boolean deep = true;
		}
	}
	/** 数值：整数/长整/浮点/双精度，含范围限制与颜色值（{@code Integer} + {@link ColorValue}）。 */
	public static class Numbers {
		@IntRange(min = 0, max = 100) public int percent = 50;
		@LongRange(min = 0) public long seed = 123456789L;
		@FloatRange(min = 0f, max = 1f) public float scale = 0.5f;
		@DoubleRange(min = 0.0, max = 10.0) public double multiplier = 1.5;
		@ColorValue public int accentColor = 0x3C8527;
		@ColorValue public int overlayColor = 0x801E1E1F;
	}
	/** 文本/枚举/列表/坐标/按键。 */
	public static class Choices {
		@StringLength(min = 1, max = 32) public String name = "flexconfig";
		public Theme theme = Theme.Dark;
		public List<String> keywords = new ArrayList<>(List.of("ore", "ui", "demo"));
		public List<Theme> themeCycle = new ArrayList<>(List.of(Theme.Dark, Theme.Light));
		public Point offset = new Point(0, 0);
		public Key toggleKey = InputConstants.UNKNOWN;
	}
	/** 排序、条件、作弊标记等元数据。 */
	public static class Extras {
		@Order(-1) public boolean appearsFirst = true;
		@Order(1) @RequiresRestart public boolean appearsLast = false;
		@WarnCheat public boolean cheaty = false;
		@Condition(value = "minecraft") public boolean visibleWithMod = true;
		@Condition(value = "definitely_not_installed") public boolean hiddenWithoutMod = true;
	}
	public enum Theme {
		Default,
		Dark,
		Light;
	}
}
