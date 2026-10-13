package io.github.forgestove.flexconfig.client.gui.components;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
/**
 * 所有 Ore 按钮的基类：统一「松手触发」与「聚焦来源」。
 * <p>
 * 松手触发：原版按下即触发，Save / Cancel 这类一触发就关界面的来不及画出按下效果；这里 {@code onClick} 置空，
 * 改在 {@link #mouseReleased} 里判「按下与松开都落在控件上」才调 {@link #onPress()}。
 * <p>
 * 聚焦来源：鼠标点击也会给控件焦点，但聚焦环只该由键盘导航触发，于是记录 {@link #keyboardFocus}；
 * 焦点本身照常给（吞掉的话容器就不会再转发 mouseReleased，松手触发会失效）。
 */
public abstract class OreAbstractButton extends AbstractButton {
	/** 按下与松开都落在本控件上才算一次点击。 */
	private boolean armed;
	private boolean keyboardFocus;
	/** 鼠标交互期间为 true：这次交互里来的焦点不算键盘的。 */
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
	 * 记录本帧的光标请求，必须在 {@code renderWidget} 里调用。
	 * <p>
	 * 只按**悬停**判定，不能带 {@code isFocused}：点击后焦点会一直留在控件上（松手触发必需），
	 * 而渲染每帧都跑 —— 若带焦点，被聚焦的控件会每帧把光标顶成手型、盖过鼠标实际位置，移开也变不回来。
	 */
	protected void renderCursor() {
		if (wantsHandCursor()) OreCursor.hand();
	}
	/** 是否请求手型光标：默认「可用且悬停」。子类可覆写追加条件（如已选中的 tab）。 */
	protected boolean wantsHandCursor() {
		return active && isHovered();
	}
	/** 触发时机由本类接管，见 {@link #mouseReleased}。 */
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
