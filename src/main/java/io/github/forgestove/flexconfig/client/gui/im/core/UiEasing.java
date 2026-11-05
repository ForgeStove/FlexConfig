package io.github.forgestove.flexconfig.client.gui.im.core;
/** 立方贝塞尔缓动。 */
public class UiEasing {
	public static final UiEasing ENTER = new UiEasing(0.2F, 0F, 0F, 1F);
	public static final UiEasing EXIT = new UiEasing(0.3F, 0F, 1F, 1F);
	public static final UiEasing LINEAR = new UiEasing(0F, 0F, 1F, 1F);
	private final float x1, y1, x2, y2;
	private UiEasing(float x1, float y1, float x2, float y2) {
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}
	public float apply(float t) {
		if (t <= 0F) return 0F;
		if (t >= 1F) return 1F;
		if (this == LINEAR) return t;
		return (float) bezier(solveX(t), y1, y2);
	}
	@SuppressWarnings("ReturnSeparatedFromComputation")
	private double solveX(double x) {
		var s = x;
		for (var i = 0; i < 8; i++) {
			var err = bezier(s, x1, x2) - x;
			if (Math.abs(err) < 1.0E-7) return s;
			var slope = slope(s, x1, x2);
			if (Math.abs(slope) < 1.0E-6) break;
			s -= err / slope;
		}
		var lo = 0D;
		var hi = 1D;
		s = x;
		for (var i = 0; i < 40; i++) {
			var v = bezier(s, x1, x2);
			if (Math.abs(v - x) < 1.0E-7) break;
			if (v < x) lo = s;
			else hi = s;
			s = (lo + hi) / 2D;
		}
		return s;
	}
	private static double bezier(double s, double p1, double p2) {
		var u = 1D - s;
		return 3D * u * u * s * p1 + 3D * u * s * s * p2 + s * s * s;
	}
	private static double slope(double s, double p1, double p2) {
		var u = 1D - s;
		return 3D * u * u * p1 + 6D * u * s * (p2 - p1) + 3D * s * s * (1D - p2);
	}
}
