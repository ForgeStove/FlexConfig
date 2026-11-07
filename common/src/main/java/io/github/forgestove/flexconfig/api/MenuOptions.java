package io.github.forgestove.flexconfig.api;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
/** 注册时可选的菜单展示信息：左上角图标。 */
public record MenuOptions(@Nullable ResourceLocation icon) {
	public static final MenuOptions NONE = new MenuOptions(null);
}
