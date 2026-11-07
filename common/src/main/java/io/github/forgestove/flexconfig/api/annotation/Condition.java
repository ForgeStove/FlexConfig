package io.github.forgestove.flexconfig.api.annotation;
import io.github.forgestove.flexconfig.api.registration.ConfigCondition;

import java.lang.annotation.*;
/** 两个条件按逻辑与组合：{@link #value()} 指定的模组要存在（留空不检查），{@link #condition()} 的类要全部通过。 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Condition {
	String value() default "";
	Class<? extends ConfigCondition>[] condition() default {};
}
