package io.github.forgestove.flexconfig.foundation.ui.core;
/**
 * 界面使用的贴图引用，直接提供原始像素尺寸。具体资源对象通过 {@link #handle()} 交给渲染实现，
 * 界面层不需要知道它是 ResourceLocation 还是别的类型。
 */
public interface UiTexture {
	int width();
	int height();
	Object handle();
}
