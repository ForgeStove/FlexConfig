package io.github.forgestove.flexconfig.api.registration;
@FunctionalInterface
public interface ConfigSaveListener<C> {
	boolean onSave(C config);
}
