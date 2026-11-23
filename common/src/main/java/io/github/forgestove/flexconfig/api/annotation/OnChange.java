package io.github.forgestove.flexconfig.api.annotation;
import io.github.forgestove.flexconfig.api.registration.ConfigChangeHandler;

import java.lang.annotation.*;
/**
 * 保存时触发。
 * <p>与 {@link RequiresRestart} 互斥。</p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnChange {
	Class<? extends ConfigChangeHandler> value();
}
