package io.github.forgestove.flexconfig.client.gui.im.core;
/** 1.20.1 分支是 Java 17，共享界面代码不能直接用 {@link Math#clamp}，统一走这里。 */
public class UiMath {
	public static int clamp(int value, int min, int max) {
		return value < min ? min : Math.min(value, max);
	}
	public static double clamp(double value, double min, double max) {
		return value < min ? min : Math.min(value, max);
	}
	public static float clamp(float value, float min, float max) {
		return value < min ? min : Math.min(value, max);
	}
	/** 帧率无关的指数逼近：{@code decay} 越大越快。 */
	public static double smooth(double current, double target, double decay, float seconds) {
		var next = target + (current - target) * Math.exp(-decay * seconds);
		return Math.abs(target - next) < 0.5 ? target : next;
	}
}
