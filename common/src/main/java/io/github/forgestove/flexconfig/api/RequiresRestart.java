package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
/**
 * 保存时新值写入文件、运行时值保持不变，重启后运行时值才切换过去。
 * 与 {@link OnChange} 互斥。
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRestart {
	RestartType value() default RestartType.GAME;
}
