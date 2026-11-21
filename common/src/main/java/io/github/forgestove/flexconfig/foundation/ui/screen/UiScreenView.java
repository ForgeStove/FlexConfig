package io.github.forgestove.flexconfig.foundation.ui.screen;
import io.github.forgestove.flexconfig.foundation.ui.*;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiControl;
import org.jetbrains.annotations.*;

import java.util.List;
import java.util.function.Predicate;
/** 自绘界面视图基类：统一处理缩放、逻辑坐标和控件输入分派；子类只实现 {@link #renderUi}。 */
public abstract class UiScreenView implements UiView {
	protected final UiContext ui = new UiContext();
	protected final ConfigHost host;
	protected int zoom = 1, logicalWidth, logicalHeight;
	protected @Nullable UiControl pressedControl;
	private final UiText title;
	protected UiScreenView(ConfigHost host, UiText title) {
		this.host = host;
		this.title = title;
	}
	public final UiText title() {
		return title;
	}
	@Override
	public final void frame(@NotNull UiRenderTarget target, int mouseX, int mouseY, float seconds) {
		zoom = UiPrefs.zoom(host.guiWidth(), host.guiHeight());
		logicalWidth = Math.max(1, host.guiWidth() / zoom);
		logicalHeight = Math.max(1, host.guiHeight() / zoom);
		ui.beginFrame(target, seconds, logicalX(mouseX), logicalY(mouseY));
		ui.viewport(logicalWidth, logicalHeight);
		ui.painter.pushScale(zoom);
		renderUi(ui, mouseX, mouseY, seconds);
		if (showTooltip()) ui.renderTooltip();
		ui.painter.popScale();
	}
	/** 在逻辑坐标下绘制整屏；缩放已开启，tooltip 由基类最后绘制。 */
	protected abstract void renderUi(UiContext ui, int mouseX, int mouseY, float seconds);
	protected boolean showTooltip() {
		return true;
	}
	@Override
	public void resize() {}
	protected final double logicalX(double mouseX) {
		return mouseX / zoom;
	}
	protected final double logicalY(double mouseY) {
		return mouseY / zoom;
	}
	/** 按下时遍历控件；滑条、文本框这类走 dragOwner 的控件不进入 pressedControl。 */
	protected final boolean pressControl(List<? extends UiControl> controls, double mouseX, double mouseY, int button) {
		var x = logicalX(mouseX);
		var y = logicalY(mouseY);
		ui.setFocusVisible(false);
		ui.pointer(x, y);
		if (button != 0) return false;
		for (var control : controls)
			if (control.mouseClicked(ui, x, y, button)) {
				if (control.down()) pressedControl = control;
				return true;
			}
		return false;
	}
	protected final boolean releaseControl(double mouseX, double mouseY, int button) {
		var x = logicalX(mouseX);
		var y = logicalY(mouseY);
		ui.pointer(x, y);
		if (pressedControl != null) {
			var control = pressedControl;
			pressedControl = null;
			control.mouseReleased(ui, x, y, button);
			return true;
		}
		if (ui.dragOwner() instanceof UiControl control) {
			var handled = control.mouseReleased(ui, x, y, button);
			ui.setDragOwner(null);
			return handled;
		}
		return false;
	}
	protected final boolean dragControl(double mouseX, double mouseY, int button, double dragX, double dragY) {
		var x = logicalX(mouseX);
		var y = logicalY(mouseY);
		ui.pointer(x, y);
		return ui.dragOwner() instanceof UiControl control && control.mouseDragged(ui, x, y, button, dragX, dragY);
	}
	/** 绘制焦点框；过滤掉不需要描边的控件（例如正在编辑的文本框）。 */
	protected final void renderFocusRing(UiPainter painter, Predicate<UiControl> filter) {
		var current = ui.focus.current();
		if (ui.focusVisible() && current instanceof UiControl control && filter.test(control))
			painter.border(control.focusBounds().inset(-1), ui.accent().light());
	}
	protected final void renderFocusRing(UiPainter painter) {
		renderFocusRing(painter, ignored -> true);
	}
}
