package io.github.forgestove.flexconfig.platform;
/**
 * 加载器侧的 {@link Platform} 选择点。本文件只存在于 Fabric 构建（见 {@code src/fabric/java}），
 * NeoForge 构建里由 {@code src/neoforge/java} 下的同名类顶替。
 * <p>
 * 这样共享源码的 {@code Platform.Holder} 不必写加载器判断，也不必依赖 Stonecutter 的条件常量。
 */
final class PlatformLoader {
	static Platform load() {
		return new FabricPlatform();
	}
}
