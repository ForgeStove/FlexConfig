package io.github.forgestove.flexconfig.client.gui.components;
/** 滚动条作用的对象：内容高度与滚动量的写入口，滚动量的来源仍是滚动条自身。 */
public interface ScrollTarget {
	int contentHeight();
	void applyScroll(double scroll);
}
