package io.github.forgestove.flexconfig.api;
@FunctionalInterface
public interface ConfigSaveListener<C> {
	boolean onSave(C config);
}
