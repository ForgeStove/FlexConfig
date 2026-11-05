package io.github.forgestove.flexconfig.api;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
/** 注册时可选的菜单展示信息：左上角图标和全局开关。 */
public record MenuOptions(@Nullable ResourceLocation icon, @Nullable Toggle toggle) {
	public static final MenuOptions NONE = new MenuOptions(null, null);
	public record Toggle(Component label, @Nullable Component tooltip, String fieldPath, BooleanSupplier state, Consumer<Boolean> setter) {}
}
