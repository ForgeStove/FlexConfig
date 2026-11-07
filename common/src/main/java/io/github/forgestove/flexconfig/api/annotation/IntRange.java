package io.github.forgestove.flexconfig.api.annotation;
import java.lang.annotation.*;
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IntRange {
	int min() default Integer.MIN_VALUE;
	int max() default Integer.MAX_VALUE;
	/** 用滑块编辑；min 和 max 都要显式写出，默认值是类型的上下限，滑块无法使用。 */
	boolean slider() default false;
	/** 滑块步长；0 表示按范围自动取 1%。 */
	int step() default 0;
}
