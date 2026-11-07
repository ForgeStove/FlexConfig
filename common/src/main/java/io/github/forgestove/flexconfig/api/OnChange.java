package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
/**
 * 值发生变化的副作用入口：触发时机是保存（保存按钮 / 快捷键落盘），逐字符编辑与磁盘加载都不触发。
 * 与 {@link RequiresRestart} 互斥。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnChange {
	Class<? extends ConfigChangeHandler> value();
}
