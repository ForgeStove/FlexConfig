package io.github.forgestove.flexconfig.client.ui.row;
/** 行加入或移出列表时的过渡动画；未实现的行直接出现或消失。 */
public interface RowTransition {
	void onEnter();
	void onExit();
	boolean exited();
}
