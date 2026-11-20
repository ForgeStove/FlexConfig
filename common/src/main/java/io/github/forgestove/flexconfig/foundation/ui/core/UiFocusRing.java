package io.github.forgestove.flexconfig.foundation.ui.core;
/** 键盘焦点框。位置跟随当前焦点控件，焦点切换时平滑移动。 */
public final class UiFocusRing {
	private final UiAnim x = new UiAnim(0, UiMotion.INDICATOR_LEAD);
	private final UiAnim y = new UiAnim(0, UiMotion.INDICATOR_LEAD);
	private final UiAnim width = new UiAnim(0, UiMotion.INDICATOR_LEAD);
	private final UiAnim height = new UiAnim(0, UiMotion.INDICATOR_LEAD);
	private final UiTween alpha = new UiTween(0);
	private boolean initialized;
	public void render(UiContext ui) {
		var current = ui.focus.current();
		if (current == null || !ui.focusVisible()) {
			initialized = false;
			alpha.start(alpha.value(), 0, UiMotion.TOOLTIP_FADE);
			alpha.update(ui.delta);
			return;
		}
		if (alpha.target() < 1) alpha.start(alpha.value(), 1, UiMotion.TOOLTIP_FADE);
		alpha.update(ui.delta);
		var bounds = current.focusBounds().inset(-1);
		if (!initialized) {
			x.jumpTo(bounds.x());
			y.jumpTo(bounds.y());
			width.jumpTo(bounds.width());
			height.jumpTo(bounds.height());
			initialized = true;
		} else {
			x.target(bounds.x());
			y.target(bounds.y());
			width.target(bounds.width());
			height.target(bounds.height());
			x.update(ui.delta);
			y.update(ui.delta);
			width.update(ui.delta);
			height.update(ui.delta);
		}
		if (alpha.value() <= 0.01F) return;
		ui.painter.pushAlpha(alpha.value());
		var left = (int) Math.round(x.get());
		var top = (int) Math.round(y.get());
		var w = (int) Math.round(width.get());
		var h = Math.max(1, (int) Math.round(height.get()));
		ui.painter.border(left - 1, top - 1, w + 2, h + 2, 0x80000000);
		ui.painter.border(left, top, w, h, ui.accent().light());
		ui.painter.popAlpha();
	}
}
