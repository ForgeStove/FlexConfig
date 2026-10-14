package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DoubleRange {
	double min() default -Double.MAX_VALUE;
	double max() default Double.MAX_VALUE;
	/** 用滑块编辑；要求 min 与 max **都显式写出来**（默认值 = 类型上下限，那种范围滑块没法用）。 */
	boolean slider() default false;
}
