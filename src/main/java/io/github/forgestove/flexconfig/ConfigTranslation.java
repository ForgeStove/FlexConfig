package io.github.forgestove.flexconfig;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
/** 配置键的显示；缺少翻译时直接显示键名，避免掩盖漏翻。 */
public class ConfigTranslation {
	public static MutableComponent translatable(String key) {
		return Component.translatable(key);
	}
}
