package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
/** 标记包含分类 POJO 的字段。 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Category {
	/** 在配置界面里是否默认展开（子分类可点标题折叠 / 展开）。 */
	boolean value() default true;
}
