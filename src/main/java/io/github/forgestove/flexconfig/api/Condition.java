package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
/** 配置分类或字段的加载条件；{@link #value()} 与 {@link #condition()} 是逻辑与。 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Condition {
	/** 仅当该模组存在时加载；留空表示不检查模组。 */
	String value() default "";
	/** 需全部返回 true 的条件类，须有无参构造。 */
	Class<? extends ConfigCondition>[] condition() default {};
}
