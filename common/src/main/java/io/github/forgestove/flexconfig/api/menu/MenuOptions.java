package io.github.forgestove.flexconfig.api.menu;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
public record MenuOptions(@Nullable ResourceLocation icon) {
	public static final MenuOptions NONE = new MenuOptions(null);
}
