package io.github.forgestove.flexconfig.client.gui.components;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
/**
 * Ore 按钮基类：统一松手触发与聚焦来源。松手触发是因为原版按下即触发，Save / Cancel 这类一触发就关界面的
 * 来不及画出按下效果，故 {@code onClick} 置空、改在 {@link #mouseReleased} 里判「按下与松开都落在控件上」。
 * 聚焦照常授予（吞掉的话容器不再转发 mouseReleased），但聚焦环只认键盘导航，故另记 {@link #keyboardFocus}。
 */
public abstract class OreAbstractButton extends AbstractButton {
	private boolean armed;
	private boolean keyboardFocus;
	/** 鼠标交互期间为 true：这期间获得的焦点不算键盘导航。 */
	private boolean mouseDriven;
	protected OreAbstractButton(int x, int y, int width, int height, @NotNull Component message) {
		super(x, y, width, height, message);
	}
	protected boolean isPressed() {
		return armed && isHovered();
	}
	protected boolean shouldRenderFocus() {
		return isFocused() && keyboardFocus;
	}
	/**
	 * 记录本帧的光标请求，须在 {@code renderWidget} 中调用。只看悬停、不带 {@code isFocused}：点击后焦点会一直
	 * 留在控件上（松手触发必需），而渲染每帧都跑，带焦点会让被聚焦的控件每帧把光标设为手型，移开也变不回来。
	 */
	protected void renderCursor() {
		if (wantsHandCursor()) OreCursor.hand();
	}
	protected boolean wantsHandCursor() {
		return active && isHovered();
	}
	@Override
	public void onClick(double mouseX, double mouseY) {}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		armed = active && isValidClickButton(button) && isMouseOver(mouseX, mouseY);
		mouseDriven = true;
		return super.mouseClicked(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (armed && isMouseOver(mouseX, mouseY)) onPress();
		armed = false;
		mouseDriven = false;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	@Override
	public void setFocused(boolean focused) {
		if (focused) keyboardFocus = !mouseDriven;
		super.setFocused(focused);
	}
	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narration) {
		defaultButtonNarrationText(narration);
	}
}
