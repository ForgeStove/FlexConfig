package io.github.forgestove.flexconfig.foundation.demo;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.flexconfig.FlexConfig;
import io.github.forgestove.flexconfig.api.CategoryIcon;
import io.github.forgestove.flexconfig.api.annotation.*;

import java.awt.Point;
import java.util.*;
/** 演示配置：每种字段类型和注解各用一次。 */
@Config(FlexConfig.ID)
@SuppressWarnings("unused")
public class DemoConfig {
	@Category(icon = CategoryIcon.GENERAL) public Appearance appearance = new Appearance();
	@Category(icon = CategoryIcon.CONTROLS) public Behavior behavior = new Behavior();
	@Category(icon = CategoryIcon.RESOURCE) public Collections collections = new Collections();
	@Category(icon = CategoryIcon.EXPERIMENTAL) public Advanced advanced = new Advanced();
	public enum Theme {
		Default,
		Dark,
		Light
	}
	/** 超过四个值走下拉框而不是分段按钮。 */
	public enum Corner {
		TopLeft,
		TopRight,
		BottomLeft,
		BottomRight,
		Center
	}
	public static class Appearance {
		@Order(-1) public Theme theme = Theme.Dark;
		@ColorValue public int accentColor = 0x3C8527;
		@ColorValue(true) public int overlayColor = 0x801E1E1F;
		@IntRange(min = 0, max = 100, slider = true, step = 5) public int opacity = 100;
		@DoubleRange(min = 0.5, max = 2.0, slider = true, step = 0.05) public double uiScale = 1.0;
		@FloatRange(min = 0f, max = 4f, slider = true) public float animationSpeed = 1f;
		public Point offset = new Point(0, 0);
		@Order(1) public boolean showTooltips = true;
	}
	public static class Behavior {
		public boolean enabled = true;
		@RequiresRestart public boolean heavyFeature = false;
		@OnChange(DemoChangeHandler.class) public boolean livePreview = true;
		@WarnCheat public boolean debugOverlay = false;
		@Condition(value = FlexConfig.ID) public boolean modGated = true;
		public Key toggleKey = InputConstants.UNKNOWN;
		public Character prefix = '#';
		@Category public Nested nested = new Nested();
		public static class Nested {
			public boolean deep = true;
			@Order(1) public int depth = 2;
		}
	}
	public static class Collections {
		@StringLength(min = 1, max = 32) public String name = "flexconfig";
		public List<String> keywords = new ArrayList<>(List.of("flex", "ui"));
		public List<Theme> themeCycle = new ArrayList<>(List.of(Theme.Dark, Theme.Light));
		public int[] weights = {1, 2, 3};
		public String[] tags = {"flex", "config"};
		public List<List<Integer>> matrix = new ArrayList<>(List.of(new ArrayList<>(List.of(1, 2))));
		public List<Rule> rules = new ArrayList<>(List.of(new Rule("first", 1)));
		public static class Rule {
			public String name;
			public int value;
			@Category public Extra extra = new Extra();
			public Rule() {}
			public Rule(String name, int value) {
				this.name = name;
				this.value = value;
			}
			public static class Extra {
				public boolean enabled = true;
			}
		}
	}
	public static class Advanced {
		@LongRange(min = 0, max = 1_000_000) public long seed = 123456789L;
		@IntRange(min = 1, max = 5) public int precision = 2;
		@StringLength(max = 64) public String description = "UI demo";
		public Corner corner = Corner.Center;
	}
}
