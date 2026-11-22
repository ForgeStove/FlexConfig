package io.github.forgestove.flexconfig.api.annotation;
import io.github.forgestove.flexconfig.api.RestartType;

import java.lang.annotation.*;
/**
 * 保存时触发。
 * <p>与 {@link OnChange} 互斥。</p>
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRestart {
	RestartType value() default RestartType.GAME;
}
