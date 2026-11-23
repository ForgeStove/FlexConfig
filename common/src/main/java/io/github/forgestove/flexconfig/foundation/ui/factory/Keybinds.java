package io.github.forgestove.flexconfig.foundation.ui.factory;
import java.util.Objects;
public class Keybinds {
	private static KeybindSupport support;
	public static void install(KeybindSupport support) {
		Keybinds.support = Objects.requireNonNull(support, "KeybindSupport");
	}
	public static KeybindSupport support() {
		if (support == null) throw new IllegalStateException("KeybindSupport is not installed");
		return support;
	}
}
