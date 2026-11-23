package io.github.forgestove.flexconfig.api.registration;
import org.jetbrains.annotations.Nullable;
@FunctionalInterface
public interface ConfigValidator<C> {
	@Nullable String validate(C config);
}
