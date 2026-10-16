package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IntRange {
	int min() default Integer.MIN_VALUE;
	int max() default Integer.MAX_VALUE;
	/** 用滑块编辑；min 与 max 须显式写出——默认值取类型上下限，滑块无法使用。 */
	boolean slider() default false;
}
