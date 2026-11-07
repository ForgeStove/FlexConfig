package io.github.forgestove.flexconfig.foundation.ui.core;
import io.github.forgestove.flexconfig.foundation.ui.core.UiMotion.SpringSpec;
/** 弹簧驱动的单值动画，供悬停、开关和滑条滑块使用。 */
public class UiAnim {
	private final UiSpring spring;
	public UiAnim(double initial) {
		this(initial, UiMotion.HOVER_IN);
	}
	public UiAnim(double initial, SpringSpec spec) {
		spring = new UiSpring(spec, (float) initial);
	}
	public double get() {
		return spring.value();
	}
	public double target() {
		return spring.target();
	}
	public double velocity() {
		return spring.velocity();
	}
	public void target(double newTarget) {
		spring.setTarget((float) newTarget);
	}
	public void retune(SpringSpec spec) {
		spring.retune(spec);
	}
	public void jumpTo(double newValue) {
		spring.snap((float) newValue);
	}
	public void snap(double newValue) {
		spring.snap((float) newValue);
	}
	public void update(float seconds) {
		spring.update(seconds);
	}
}
