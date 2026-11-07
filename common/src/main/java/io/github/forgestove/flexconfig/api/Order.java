package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
/** 字段在其声明类中的排列顺序，按值升序；未标注的按 0 处理，同值时保持声明顺序。 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {
	int value() default 0;
}
