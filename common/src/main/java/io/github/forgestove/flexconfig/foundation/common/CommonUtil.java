package io.github.forgestove.flexconfig.foundation.common;
import io.github.forgestove.flexconfig.FlexConfig;
import net.minecraft.resources.ResourceLocation;
public class CommonUtil {
	public static ResourceLocation getFlexRes(String path) {
		return new ResourceLocation(FlexConfig.ID, path);
	}
}
