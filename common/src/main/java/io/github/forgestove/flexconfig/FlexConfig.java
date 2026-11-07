package io.github.forgestove.flexconfig;
import net.minecraft.resources.ResourceLocation;
public class FlexConfig {
	public static final String ID = "flexconfig";
	public static final String NETWORK_VERSION = "1";
	public static ResourceLocation getRes(String path) {
		return ResourceLocation.withDefaultNamespace(path);
	}
	public static ResourceLocation getFlexRes(String path) {
		return ResourceLocation.fromNamespaceAndPath(ID, path);
	}
}
