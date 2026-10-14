package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FloatRange {
	float min() default -Float.MAX_VALUE;
	float max() default Float.MAX_VALUE;
	/** 用滑块编辑；要求 min 与 max **都显式写出来**（默认值 = 类型上下限，那种范围滑块没法用）。 */
	boolean slider() default false;
}
