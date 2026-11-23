package io.github.forgestove.flexconfig.api.annotation;
import io.github.forgestove.flexconfig.api.ConfigType;

import java.lang.annotation.*;
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Config {
	String value();
	ConfigType type() default ConfigType.CLIENT;
}
