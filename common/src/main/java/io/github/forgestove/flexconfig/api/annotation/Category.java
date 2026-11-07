package io.github.forgestove.flexconfig.api.annotation;
import io.github.forgestove.flexconfig.api.CategoryIcon;

import java.lang.annotation.*;
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Category {
	/** 侧边栏图标，默认不显示。 */
	CategoryIcon icon() default CategoryIcon.NONE;
}
