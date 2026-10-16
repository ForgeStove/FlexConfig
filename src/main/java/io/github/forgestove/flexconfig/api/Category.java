package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Category {
	/** 分类标题初始是否展开；界面中顶层分类固定展开，该值目前只在子分类上生效。 */
	boolean value() default true;
}
