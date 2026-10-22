package io.github.forgestove.flexconfig.client.gui.components;
public interface ScrollTarget {
	int contentHeight();
	int maxScroll();
	void applyScroll(double scroll);
}
