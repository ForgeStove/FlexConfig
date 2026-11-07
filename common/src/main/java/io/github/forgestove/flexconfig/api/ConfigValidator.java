package io.github.forgestove.flexconfig.api;
import org.jetbrains.annotations.Nullable;
@FunctionalInterface
public interface ConfigValidator<C> {
	@Nullable String validate(C config);
}
