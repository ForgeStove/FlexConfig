package io.github.forgestove.flexconfig.foundation.common;
import io.github.forgestove.flexconfig.FlexConfig;
import net.minecraft.resources.ResourceLocation;
public class CommonUtil {
	public static ResourceLocation getFlexRes(String path) {
		return ResourceLocation.fromNamespaceAndPath(FlexConfig.ID, path);
	}
}
