package io.github.forgestove.flexconfig.api.annotation;
import java.lang.annotation.*;
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DoubleRange {
	double min() default -Double.MAX_VALUE;
	double max() default Double.MAX_VALUE;
	boolean slider() default false;
	double step() default 0;
}
