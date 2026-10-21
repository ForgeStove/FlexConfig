package io.github.forgestove.flexconfig;
import net.minecraft.network.chat.Component;
/** 配置键的显示：没有翻译时退化为键的末段，tooltip 键保留 "名字.tooltip"，不显示整条键。 */
public final class ConfigTranslation {
	public static Component translatable(String key) {
		return Component.translatableWithFallback(key, fallback(key));
	}
	private static String fallback(String key) {
		var end = key.endsWith(".tooltip") ? key.length() - ".tooltip".length() : key.length();
		return key.substring(key.lastIndexOf('.', end - 1) + 1);
	}
}
