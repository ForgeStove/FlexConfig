package io.github.forgestove.flexconfig.api.annotation;
import io.github.forgestove.flexconfig.api.ConfigType;

import java.lang.annotation.*;
/** 标记配置类：模组 ID 与类型由注解声明，其余注册信息由 {@code ConfigHandler.Builder} 提供。 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Config {
	/** 模组 ID，同时用作翻译键前缀。 */
	String value();
	ConfigType type() default ConfigType.CLIENT;
}
