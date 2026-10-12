package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
/**
 * 指定字段在其声明类中的排列顺序，按值升序。
 * <p>
 * 未标注的按 0 处理，彼此保持 {@link Class#getDeclaredFields()} 的相对顺序。
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Order {
	int value() default 0;
}
