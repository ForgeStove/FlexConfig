package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
/** 将整数字段标记为颜色值，在配置界面显示颜色选择器。 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ColorValue {
	/** 颜色是否含 Alpha 通道（ARGB）；false 表示仅 RGB。 */
	boolean value() default false;
}
