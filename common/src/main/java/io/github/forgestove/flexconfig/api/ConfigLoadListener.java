package io.github.forgestove.flexconfig.api;
@FunctionalInterface
public interface ConfigLoadListener<C> {
	boolean onLoad(C config);
}
