package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Config {
	/** 模组 ID，同时用作翻译键前缀。 */
	String value();
	ConfigType type() default ConfigType.CLIENT;
	/** 显式文件名（{@code .toml} 可省略）；留空按 {@code <modId>-<type>.toml} 推导。 */
	String fileName() default "";
}
