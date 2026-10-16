package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
/** 两个元素是逻辑与：{@link #value()} 指定的模组须存在（留空则不检查），{@link #condition()} 的条件类须全部通过。 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Condition {
	String value() default "";
	Class<? extends ConfigCondition>[] condition() default {};
}
