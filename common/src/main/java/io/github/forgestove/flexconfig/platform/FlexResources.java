package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.FlexConfig;
import net.minecraft.resources.ResourceLocation;
/** 把 {@link FlexConfig} 的命名空间标识落地成 Minecraft 资源，网络层和贴图共用同一套规则。 */
public class FlexResources {
	public static ResourceLocation getFlexRes(String path) {
		var res = FlexConfig.getFlexRes(path);
		return ResourceLocation.fromNamespaceAndPath(res.namespace(), res.path());
	}
}
