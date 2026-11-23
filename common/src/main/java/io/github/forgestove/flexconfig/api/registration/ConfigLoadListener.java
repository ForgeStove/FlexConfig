package io.github.forgestove.flexconfig.api.registration;
@FunctionalInterface
public interface ConfigLoadListener<C> {
	boolean onLoad(C config);
}
