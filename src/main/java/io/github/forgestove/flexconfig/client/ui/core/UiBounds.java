package io.github.forgestove.flexconfig.client.ui.core;
public record UiBounds(int x, int y, int width, int height) {
	public static final UiBounds EMPTY = new UiBounds(0, 0, 0, 0);
	public int right() {
		return x + width;
	}
	public int bottom() {
		return y + height;
	}
	public boolean contains(double px, double py) {
		return px >= x && px < right() && py >= y && py < bottom();
	}
	public boolean intersects(UiBounds other) {
		return right() > other.x && x < other.right() && bottom() > other.y && y < other.bottom();
	}
	public UiBounds inset(int amount) {
		return inset(amount, amount);
	}
	public UiBounds inset(int dx, int dy) {
		return new UiBounds(x + dx, y + dy, Math.max(0, width - dx * 2), Math.max(0, height - dy * 2));
	}
	public UiBounds offset(int dx, int dy) {
		return new UiBounds(x + dx, y + dy, width, height);
	}
}
