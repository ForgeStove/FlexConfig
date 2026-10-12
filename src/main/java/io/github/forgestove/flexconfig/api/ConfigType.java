package io.github.forgestove.flexconfig.api;
import java.util.Locale;
/** 配置类型，决定加载时机、存放目录与是否同步；默认文件名都是 {@code <modId>-<类型>.toml}。 */
public enum ConfigType {
	/** 双端加载，存全局配置目录，不同步。 */
	COMMON,
	/** 只在客户端加载，存全局配置目录，不同步。 */
	CLIENT,
	/** 随服务器启动加载，存存档的 {@code serverconfig}（缺省回退全局目录），连接时下发给客户端。 */
	SERVER;
	public String extension() {
		return name().toLowerCase(Locale.ROOT);
	}
}
