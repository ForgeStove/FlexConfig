package io.github.forgestove.flexconfig.foundation;
import com.demonwav.mcdev.annotations.Translatable;
import net.minecraft.network.chat.*;
/** 配置键的显示；缺少翻译时直接显示键名，避免掩盖漏翻。 */
public class ConfigTranslation {
	public static MutableComponent translatable(@Translatable(foldMethod = true) String key) {
		return Component.translatable(key);
	}
}
