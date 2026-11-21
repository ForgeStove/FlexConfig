package io.github.forgestove.flexconfig;
public class FlexConfig {
	public static final String ID = "flexconfig";
	public static final String NETWORK_VERSION = "1";
	public static Res getFlexRes(String path) {
		return new Res(ID, path);
	}
	/** 与加载器和 Minecraft 都无关的资源标识，由平台层转换成各自的资源对象。 */
	public record Res(String namespace, String path) {}
}
