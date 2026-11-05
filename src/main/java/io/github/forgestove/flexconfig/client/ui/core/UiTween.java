package io.github.forgestove.flexconfig.client.ui.core;
import io.github.forgestove.flexconfig.client.ui.core.UiMotion.TweenSpec;
/** 带延迟和缓动的补间。 */
public class UiTween {
	private float from, to, durationMs, delayMs, elapsedMs;
	private UiEasing easing = UiEasing.LINEAR;
	public UiTween(float initial) {
		snap(initial);
	}
	public void start(float from, float to, TweenSpec spec) {
		start(from, to, spec.durationMs(), spec.easing(), 0F);
	}
	public void start(float to, TweenSpec spec) {
		start(value(), to, spec);
	}
	public void start(float from, float to, float durationMs, UiEasing easing, float delayMs) {
		this.from = from;
		this.to = to;
		this.durationMs = Math.max(0F, durationMs);
		this.delayMs = Math.max(0F, delayMs);
		elapsedMs = 0F;
		this.easing = easing == null ? UiEasing.LINEAR : easing;
	}
	public void snap(float v) {
		from = to = v;
		durationMs = delayMs = elapsedMs = 0F;
	}
	public void update(float dt) {
		if (UiMotion.reducedMotion()) {
			elapsedMs = delayMs + durationMs;
			return;
		}
		if (!done()) elapsedMs += Math.max(0F, dt) * 1000F;
	}
	public boolean done() {
		return elapsedMs >= delayMs + durationMs;
	}
	public float target() {
		return to;
	}
	public float value() {
		return from + (to - from) * easing.apply(progress());
	}
	public float progress() {
		if (elapsedMs <= delayMs) return durationMs <= 0F && elapsedMs >= delayMs ? 1F : 0F;
		return durationMs <= 0F ? 1F : Math.min(1F, (elapsedMs - delayMs) / durationMs);
	}
}
