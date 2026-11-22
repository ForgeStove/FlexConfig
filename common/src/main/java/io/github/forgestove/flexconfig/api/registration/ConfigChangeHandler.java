package io.github.forgestove.flexconfig.api.registration;
@FunctionalInterface
public interface ConfigChangeHandler {
	void onChange(Object oldValue, Object newValue);
}
