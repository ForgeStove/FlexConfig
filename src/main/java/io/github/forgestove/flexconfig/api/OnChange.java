package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
/**
 * 标记「值被改掉之后要执行副作用」，值确实变化时框架调用 {@link ConfigChangeHandler}。
 * <p>
 * 触发时机是保存时（保存按钮 / 快捷键落盘），不是逐字符编辑，磁盘加载也不会触发。
 * 与 {@link RequiresRestart} 互斥（那个是改完不生效、要重启）。
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OnChange {
	/** 值变化后调用的处理器，需有无参构造。 */
	Class<? extends ConfigChangeHandler> value();
}
