package io.github.forgestove.flexconfig.foundation.ui.screen;
import io.github.forgestove.flexconfig.foundation.client.ClientUtil;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiControl;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;
/** 自绘界面基类：统一处理缩放、UiContext 帧和逻辑坐标换算；子类只实现 {@link #renderUi}，输入按需转发给控件。 */
public abstract class UiScreen extends Screen {
	protected final UiContext ui = new UiContext();
	protected int zoom = 1, logicalWidth, logicalHeight;
	protected @Nullable UiControl pressedControl;
	protected UiScreen(Component title) {
		super(title);
	}
	/** 在缩放开启前绘制屏幕级底图（模糊背景等）。 */
	protected void renderBeforeScale(int mouseX, int mouseY, float delta) {}
	/** 在逻辑坐标下绘制整屏；缩放已开启，tooltip 由基类最后绘制。 */
	protected abstract void renderUi(UiContext ui, int mouseX, int mouseY, float delta);
	protected boolean showTooltip() {
		return true;
	}
	@Override
	public final void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		zoom = UiPrefs.zoom(width, height);
		logicalWidth = Math.max(1, width / zoom);
		logicalHeight = Math.max(1, height / zoom);
		ui.beginFrame(graphics, ClientUtil.mc.getTimer().getRealtimeDeltaTicks() / 20F, logicalX(mouseX), logicalY(mouseY));
		ui.viewport(logicalWidth, logicalHeight);
		renderBeforeScale(mouseX, mouseY, delta);
		ui.painter.pushScale(zoom);
		renderUi(ui, mouseX, mouseY, delta);
		if (showTooltip()) ui.renderTooltip();
		ui.painter.popScale();
	}
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
