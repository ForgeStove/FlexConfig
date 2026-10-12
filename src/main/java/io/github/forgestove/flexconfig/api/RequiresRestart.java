package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
/**
 * 标记该字段的改动需要重启才生效：保存时新值只写入文件，运行时值保持不变直到重启发生。
 * <p>
 * 界面按 {@link RestartType} 给出对应提示。与 {@link OnChange} 互斥（那个是改完即生效）。
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRestart {
	/** 生效所需的动作，默认重启整个游戏。 */
	RestartType value() default RestartType.GAME;
}
