package io.github.forgestove.flexconfig.client.gui.api;
import io.github.forgestove.flexconfig.client.gui.ConfigCategoryTab;
/** 入口加入 ConfigCategoryTab 之后回调，回调注册一类初始化在此进行。 */
public interface TabLifecycle {
	void onAttachedToTab(ConfigCategoryTab<?, ?> tab);
}
