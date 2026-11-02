package io.github.forgestove.flexconfig.platform;
/**
 * 加载器侧的 {@link Platform} 选择点。本文件只存在于 Forge 构建（见 {@code src/forge/java}），
 * 其他加载器由各自源集下的同名类顶替。
 */
class PlatformLoader {
	static Platform load() {
		return new ForgePlatform();
	}
}
