package io.github.forgestove.flexconfig.client.gui.components;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
/** 竖直滚动条：滚动量、滑块位置、命中、拖动、翻页与平滑，只在内容超过一屏时出现。 */
public class FlexScrollBar extends AbstractWidget {
	public static final int WIDTH = 6;
	/** 滑块的最小高度与滚轮一次的默认距离（列表两行高：16 + 2）。 */
	private static final int MIN_KNOB_H = 12, DEFAULT_STEP = 36;
	/** 每秒的平滑速度系数：{@code 7} 为比例项，{@code 200} 为最小速度，保证收尾不顿挫。 */
	private static final double RATIO = 7, MIN_SPEED = 200;
	private static final ResourceLocation TRACK = ResourceLocation.withDefaultNamespace("widget/scroller_background");
	private static final ResourceLocation KNOB = ResourceLocation.withDefaultNamespace("widget/scroller");
	/** 当前与目标滚动量：渲染用前者，拖动与翻页写后者，由 {@link #update} 逼近。 */
	private double scroll, target;
	/** 滚轮一格的距离；按行滚动的界面设为整行像素高。 */
	private double step = DEFAULT_STEP;
	/** 鼠标按下时相对滑块顶端的偏移，拖动时按它还原滑块位置。 */
	private double grab;
	private boolean dragging;
	private int content;
	public FlexScrollBar() {
		super(0, 0, WIDTH, 0, Component.empty());
	}
	/** 可视区变化后、其余调用之前调一次。 */
	public void area(int x, int y, int height, int content) {
		setX(x);
		setY(y);
		setWidth(WIDTH);
		setHeight(height);
		this.content = content;
	}
	public double scroll() {
		return scroll;
	}
	public void step(double step) {
		this.step = step;
	}
	/** 每帧渲染内容之前调一次。 */
	public void update() {
		var max = Math.max(0, content - getHeight());
		target = Math.clamp(target, 0, max);
		scroll = Math.clamp(scroll, 0, max);
		var seconds = Minecraft.getInstance().getTimer().getRealtimeDeltaTicks() / 20.0;
		var rest = target - scroll;
		var step = Math.signum(rest) * Math.max(MIN_SPEED, Math.abs(rest) * RATIO) * seconds;
		scroll = Math.abs(step) >= Math.abs(rest) ? target : scroll + step;
	}
	/** {@code amount} 为滚轮格数，正数向上；一格距离由 {@link #step} 决定。 */
	public void wheel(double amount) {
		target -= amount * step;
	}
	@Override
	protected void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		if (!shown()) return;
		gui.blitSprite(TRACK, getX(), getY(), WIDTH, getHeight());
		var h = knobHeight();
		gui.blitSprite(KNOB, getX(), knobY(h), WIDTH, h);
	}
	/** 点在滑块上为抓取并继续拖动（不跳位置），空白处为翻页。 */
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0 || !isMouseOver(mouseX, mouseY)) return false;
		var knobH = knobHeight();
		var knobY = knobY(knobH);
		if (knobY <= mouseY && mouseY < knobY + knobH) {
			dragging = true;
			grab = mouseY - knobY;
		} else scrollBy(mouseY < knobY ? -getHeight() : getHeight());
		return true;
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (!dragging) return false;
		dragging = false;
		return true;
	}
	/** 拖动中：按抓取时的偏移还原滑块位置，从鼠标按下处继续拖动。 */
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (!dragging) return false;
		var travel = getHeight() - knobHeight();
		if (travel <= 0) return false;
		// 直接落位，不经过插值：滑块按 scroll 绘制，写目标值会让鼠标移开后仍继续追赶
		scroll = target = Math.clamp((mouseY - grab - getY()) * (content - getHeight()) / (double) travel, 0, content - getHeight());
		return true;
	}
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return shown() && super.isMouseOver(mouseX, mouseY);
	}
	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {}
	/** 内容不超出一屏时整条不绘制也不响应事件。 */
	public boolean shown() {
		return content > getHeight();
	}
	private int knobHeight() {
		return content > getHeight() ? Math.max(MIN_KNOB_H, getHeight() * getHeight() / content) : 0;
	}
	private int knobY(int knobH) {
		return getY() + (int) ((getHeight() - knobH) * (scroll / (content - getHeight())));
	}
	public void scrollBy(double delta) {
		target += delta;
	}
}
