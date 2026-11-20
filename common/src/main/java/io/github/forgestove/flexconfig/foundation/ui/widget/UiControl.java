package io.github.forgestove.flexconfig.foundation.ui.widget;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.core.UiFocus.Target;
import org.jetbrains.annotations.Nullable;
public abstract class UiControl implements Target, UiPointerTarget {
	protected boolean enabled = true, visible = true;
	protected boolean down;
	private UiBounds bounds = UiBounds.EMPTY;
	private UiBounds pressBounds = UiBounds.EMPTY;
	private UiText tooltip;
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
	@Override
	public void markPressed() {
		pressBounds = bounds;
	}
	/** 释放时同时检查当前 bounds 和按下时的 bounds，列表在按压期间重排也不会丢动作。 */
	public boolean releasedWithin(double mouseX, double mouseY) {
		return bounds.contains(mouseX, mouseY) || pressBounds.contains(mouseX, mouseY);
	}
	public void tooltip(@Nullable UiText tooltip) {
		this.tooltip = tooltip;
	}
	public @Nullable UiText tooltip() {
		return tooltip;
	}
	public boolean hovered(UiContext ctx) {
		return visible && bounds.contains(ctx.mouseX, ctx.mouseY);
	}
	public abstract void render(UiContext ctx);
	@Override
	public boolean mouseClicked(UiContext ctx, double mouseX, double mouseY, int button) {
		return false;
	}
	public boolean keyPressed(UiContext ctx, int keyCode, int scanCode, int modifiers) {
		return false;
	}
	public boolean mouseScrolled(UiContext ctx, double mouseX, double mouseY, double amount) {
		return false;
	}
	public boolean charTyped(UiContext ctx, char codePoint) {
		return false;
	}
	/** 悬停时把提示交给界面统一绘制。 */
	protected void showTooltip(UiContext ctx) {
		if (tooltip == null) return;
		if (hovered(ctx)) ctx.hover(this, this::tooltip, focusBounds());
		else if (ctx.focus.is(this)) ctx.focusTarget(this, this::tooltip, focusBounds());
	}
}
