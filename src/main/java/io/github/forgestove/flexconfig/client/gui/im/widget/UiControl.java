package io.github.forgestove.flexconfig.client.gui.im.widget;
import io.github.forgestove.flexconfig.client.gui.im.core.*;
import io.github.forgestove.flexconfig.client.gui.im.core.UiFocus.Target;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
/** 自绘控件基类：不继承原版 widget，布局和命中都在每帧完成。 */
public abstract class UiControl implements Target {
	private UiBounds bounds = UiBounds.EMPTY;
	private Component tooltip;
	protected boolean enabled = true, visible = true;
	protected boolean down;
	public UiBounds bounds() {
		return bounds;
	}
	public void setBounds(UiBounds bounds) {
		this.bounds = bounds;
	}
	@Override
	public UiBounds focusBounds() {
		return bounds;
	}
	public boolean visible() {
		return visible;
	}
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	public boolean enabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public boolean focusable() {
		return visible && enabled;
	}
	public boolean down() {
		return down;
	}
	public void tooltip(@Nullable Component tooltip) {
		this.tooltip = tooltip;
	}
	public @Nullable Component tooltip() {
		return tooltip;
	}
	public boolean hovered(UiContext ctx) {
		return visible && bounds.contains(ctx.mouseX, ctx.mouseY);
	}
	public abstract void render(UiContext ctx);
	public boolean mouseClicked(UiContext ctx, double mouseX, double mouseY, int button) {
		return false;
	}
	public boolean mouseDragged(UiContext ctx, double mouseX, double mouseY, int button, double dragX, double dragY) {
		return false;
	}
	public boolean mouseReleased(UiContext ctx, double mouseX, double mouseY, int button) {
		return false;
	}
	public boolean keyPressed(UiContext ctx, int keyCode, int scanCode, int modifiers) {
		return false;
	}
	public boolean charTyped(UiContext ctx, char codePoint) {
		return false;
	}
	/** 悬停时把提示交给界面统一绘制。 */
	protected void showTooltip(UiContext ctx) {
		if (tooltip == null) return;
		if (hovered(ctx)) ctx.tooltip(tooltip, focusBounds());
		else if (ctx.focus.is(this)) ctx.tooltipFocused(tooltip, focusBounds());
	}
}
