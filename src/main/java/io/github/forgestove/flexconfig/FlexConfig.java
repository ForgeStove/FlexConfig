package io.github.forgestove.flexconfig;
import net.minecraft.resources.ResourceLocation;
public class FlexConfig {
	public static final String ID = "flexconfig";
	public static final String NETWORK_VERSION = "1";
	/** 原版命名空间的资源位置。 */
	@SuppressWarnings("removal")
	public static ResourceLocation getRes(String path) {
		return new ResourceLocation(path);
	}
	/** 本模组命名空间的资源位置。 */
	@SuppressWarnings("removal")
	public static ResourceLocation getFlexRes(String path) {
		return new ResourceLocation(ID, path);
	}
}
