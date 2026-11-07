package io.github.forgestove.flexconfig.api;
import java.util.Locale;
public enum ConfigType {
	COMMON,
	CLIENT,
	SERVER;
	public String extension() {
		return name().toLowerCase(Locale.ROOT);
	}
}
