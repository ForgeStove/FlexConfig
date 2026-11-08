package io.github.forgestove.flexconfig.foundation.ui.core;
import io.github.forgestove.flexconfig.foundation.ui.core.UiMotion.SpringSpec;
public class SpringScrollMotion implements ScrollMotion {
	private final UiAnim anim;
	public SpringScrollMotion(SpringSpec spec) {
		this(spec, 0);
	}
	public SpringScrollMotion(SpringSpec spec, double initial) {
		anim = new UiAnim(initial, spec);
	}
	public void retune(SpringSpec spec) {
		anim.retune(spec);
	}
	@Override
	public double value() {
		return anim.get();
	}
	@Override
	public double target() {
		return anim.target();
	}
	@Override
	public void setTarget(double value) {
		anim.target(value);
	}
	@Override
	public void snap(double value) {
		anim.snap(value);
	}
	@Override
	public void update(float seconds) {
		anim.update(seconds);
	}
}
