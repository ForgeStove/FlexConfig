package io.github.forgestove.flexconfig.client.gui.im.core;
import io.github.forgestove.flexconfig.client.gui.im.core.UiMotion.SpringSpec;
/** 固定步长弹簧。 */
public class UiSpring {
	private static final float STEP = 0.004166667F, EPSILON = 0.01F;
	private float stiffness, friction, target, value, previous, velocity, accumulator;
	private boolean resting = true;
	public UiSpring(SpringSpec spec, float initial) {
		retune(spec);
		target = value = previous = initial;
	}
	public void retune(SpringSpec spec) {
		var omega = Math.PI * 2 / spec.response();
		stiffness = (float) (omega * omega);
		friction = (float) (Math.PI * 4 * spec.damping() / spec.response());
	}
	public void setTarget(float newTarget) {
		if (newTarget == target) return;
		target = newTarget;
		resting = false;
	}
	public void snap(float v) {
		target = value = previous = v;
		velocity = accumulator = 0F;
		resting = true;
	}
	public void update(float dt) {
		if (resting) return;
		if (UiMotion.reducedMotion()) {
			snap(target);
			return;
		}
		accumulator += Math.max(0F, dt);
		var steps = 0;
		while (accumulator >= STEP && steps < 60) {
			accumulator -= STEP;
			steps++;
			previous = value;
			var force = -stiffness * (value - target) - friction * velocity;
			velocity += force * STEP;
			value += velocity * STEP;
			if (Math.abs(value - target) < EPSILON && Math.abs(velocity) < EPSILON) {
				snap(target);
				return;
			}
		}
		if (steps == 60) accumulator = 0F;
	}
	public float value() {
		if (resting) return value;
		return previous + (value - previous) * (accumulator / STEP);
	}
	public float target() {
		return target;
	}
}
