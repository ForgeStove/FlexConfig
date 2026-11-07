package io.github.forgestove.flexconfig.api.annotation;
import io.github.forgestove.flexconfig.api.registration.ConfigChangeHandler;

import java.lang.annotation.*;
/**
 * 保存时触发（保存按钮或快捷键）；逐字符编辑和读取文件都不触发。
 * 与 {@link RequiresRestart} 互斥。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnChange {
	Class<? extends ConfigChangeHandler> value();
}
