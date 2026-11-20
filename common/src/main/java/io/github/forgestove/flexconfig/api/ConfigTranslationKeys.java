package io.github.forgestove.flexconfig.api;
public class ConfigTranslationKeys {
	public static String category(String modId, String path) {
		return "%s.config.category.%s".formatted(modId, path);
	}
	public static String option(String modId, String path, String name) {
		return path == null || path.isBlank() ? option(modId, name) : "%s.config.option.%s.%s".formatted(modId, path, name);
	}
	public static String option(String modId, String name) {
		return "%s.config.option.%s".formatted(modId, name);
	}
	public static String tooltip(String key) {
		return key + ".tooltip";
	}
	public static String unit(String key) {
		return key + ".unit";
	}
	public static String enumValue(String modId, Class<?> enumType, String constant) {
		return "%s.config.enum.%s.%s".formatted(modId, enumType.getSimpleName(), constant);
	}
	public static String enumValue(String modId, Enum<?> value) {
		return enumValue(modId, value.getDeclaringClass(), value.name());
	}
}
