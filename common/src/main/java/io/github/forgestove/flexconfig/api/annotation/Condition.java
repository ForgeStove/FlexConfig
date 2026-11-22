package io.github.forgestove.flexconfig.api.annotation;
import io.github.forgestove.flexconfig.api.registration.ConfigCondition;

import java.lang.annotation.*;
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Condition {
	String value() default "";
	Class<? extends ConfigCondition>[] condition() default {};
}
