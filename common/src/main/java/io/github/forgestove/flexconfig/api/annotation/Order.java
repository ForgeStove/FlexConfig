package io.github.forgestove.flexconfig.api.annotation;
import java.lang.annotation.*;
/** 字段在声明类中的排列顺序，按值升序；没有标注的按 0 参与排序，值相同时保持声明顺序。 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {
	int value() default 0;
}
