package io.github.forgestove.flexconfig.api;
import java.util.Locale;
/** 决定加载时机、存放目录与是否同步；{@code SERVER} 型存存档的 {@code serverconfig}、缺省回退全局目录，并在连接时下发给客户端。 */
public enum ConfigType {
	COMMON,
	CLIENT,
	SERVER;
	public String extension() {
		return name().toLowerCase(Locale.ROOT);
	}
}
