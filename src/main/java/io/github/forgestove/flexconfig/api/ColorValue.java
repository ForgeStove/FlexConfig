package io.github.forgestove.flexconfig.api;
import java.lang.annotation.*;
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ColorValue {
	/** {@code true} 为 ARGB，{@code false} 为 RGB。 */
	boolean value() default false;
}
