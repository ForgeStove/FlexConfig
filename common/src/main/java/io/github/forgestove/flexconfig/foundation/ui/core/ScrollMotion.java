package io.github.forgestove.flexconfig.foundation.ui.core;
public interface ScrollMotion {
	/** 当前应绘制的滚动位置。 */
	double value();
	/** 正在趋近的目标位置。 */
	double target();
	void setTarget(double value);
	/** 直接对齐到某个位置，不产生过渡。 */
	void snap(double value);
	/** 每帧推进；{@code seconds} 是帧间隔。 */
	void update(float seconds);
}
