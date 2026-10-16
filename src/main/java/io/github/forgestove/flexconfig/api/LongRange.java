package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LongRange {
	long min() default Long.MIN_VALUE;
	long max() default Long.MAX_VALUE;
	/** 用滑块编辑；min 与 max 须显式写出——默认值取类型上下限，滑块无法使用。 */
	boolean slider() default false;
}
