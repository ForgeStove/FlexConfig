package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Config {
	/** 模组 ID，同时用作翻译键前缀。 */
	String value();
	/** 配置类型，决定加载时机、存放目录与是否同步。 */
	ConfigType type() default ConfigType.CLIENT;
	/** 显式文件名（可省略 {@code .toml}）；留空按 {@code <modId>-<type>.toml} 推导。 */
	String fileName() default "";
}
