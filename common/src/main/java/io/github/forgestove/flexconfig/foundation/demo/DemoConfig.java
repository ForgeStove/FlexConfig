package io.github.forgestove.flexconfig.foundation.demo;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.flexconfig.FlexConfig;
import io.github.forgestove.flexconfig.api.*;

import java.awt.Point;
import java.util.*;
@Config(FlexConfig.ID)
@SuppressWarnings("unused")
public class DemoConfig {
	@Category public Toggles toggles = new Toggles();
	@Category public Numbers numbers = new Numbers();
	public enum Theme {
		Default,
		Dark,
		Light
	}
	public static class Toggles {
		public boolean enabled = true;
		public boolean fancyMode = false;
		@RequiresRestart public boolean heavyFeature = false;
		@Category public Nested nested = new Nested();
		@Category public Extras extras = new Extras();
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
			@Condition(value = FlexConfig.ID) public boolean visibleWithMod = true;
			@Condition(value = "definitely_not_installed") public boolean hiddenWithoutMod = true;
		}
	}
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
	public static class Choices {
		@StringLength(min = 1, max = 32) public String name = "flexconfig";
		public Theme theme = Theme.Dark;
		public List<String> keywords = new ArrayList<>(List.of("flex", "ui", "demo"));
		public List<Theme> themeCycle = new ArrayList<>(List.of(Theme.Dark, Theme.Light));
		public Point offset = new Point(0, 0);
		public Key toggleKey = InputConstants.UNKNOWN;
		public int[] weights = {1, 2, 3};
		public String[] tags = {"flex", "config"};
		public List<Rule> rules = new ArrayList<>(List.of(new Rule("first", 1), new Rule("second", 2)));
		public List<List<Integer>> matrix = new ArrayList<>(List.of(new ArrayList<>(List.of(1, 2)), new ArrayList<>(List.of(3, 4))));
		public static class Rule {
			public String name;
			public int value;
			@Category public Extra extra = new Extra();
			public Rule(String name, int value) {
				this.name = name;
				this.value = value;
			}
			public static class Extra {
				public boolean enabled = true;
			}
		}
		@StringLength(max = 64) public String description = "UI demo";
		public Theme fallbackTheme = Theme.Default;
		public Theme hoverTheme = Theme.Light;
		public List<String> aliases = new ArrayList<>(List.of("fc", "flex"));
		public Point anchor = new Point(4, 4);
		public Key resetKey = InputConstants.UNKNOWN;
	}
}
