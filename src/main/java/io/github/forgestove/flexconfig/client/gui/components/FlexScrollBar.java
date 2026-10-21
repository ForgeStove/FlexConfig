package io.github.forgestove.flexconfig.client.gui.components;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
/** 竖直滚动条：滚动量、滑块位置、命中、拖动、翻页与平滑，只在内容超过一屏时出现。 */
public class FlexScrollBar extends AbstractWidget {
	/** 滑块的最小高度与滚轮一次的默认距离（列表两行高：16 + 2）。 */
	private static final int MIN_KNOB_H = 12, DEFAULT_STEP = 36;
	/** 每秒按剩余距离衰减的系数；速度随距离缩小，没有固定速率。{@code 20·ln4} 等价于每 tick 吃掉 75% 的差距。 */
	private static final double DECAY = 27.7;
	private static final ResourceLocation TRACK = ResourceLocation.withDefaultNamespace("widget/scroller_background");
	private static final ResourceLocation KNOB = ResourceLocation.withDefaultNamespace("widget/scroller");
	private final ScrollTarget owner;
	/** 当前与目标滚动量：渲染用前者，拖动与翻页写后者，由 {@link #update} 逼近。 */
	private double scroll, target;
	/** 滚轮一格的距离；按行滚动的界面设为整行像素高。 */
	private double step = DEFAULT_STEP;
	/** 鼠标按下时相对滑块顶端的偏移，拖动时按它还原滑块位置。 */
	private double grab;
	private boolean dragging;
	/** 滚轮命中区：整个内容视口，滚轮不需要精确落在 6px 的条上。 */
	private ScreenRectangle wheelArea = ScreenRectangle.empty();
	public FlexScrollBar(ScrollTarget owner, int x, int y, int width, int height) {
		super(x, y, width, height, Component.empty());
		this.owner = owner;
	}
	public void step(double step) {
		this.step = step;
	}
	public void wheelArea(ScreenRectangle wheelArea) {
		this.wheelArea = wheelArea;
	}
	/** 每帧渲染内容之前调一次：平滑逼近目标，并把结果写给 {@link ScrollTarget}。 */
	public void update() {
		var max = Math.max(0, owner.contentHeight() - getHeight());
		target = Math.clamp(target, 0, max);
		scroll = Math.clamp(scroll, 0, max);
		var seconds = Minecraft.getInstance().getTimer().getRealtimeDeltaTicks() / 20.0;
		scroll = target + (scroll - target) * Math.exp(-DECAY * seconds);
		if (Math.abs(target - scroll) < 0.5) scroll = target;
		owner.applyScroll(scroll);
	}
	/** {@code amount} 为滚轮格数，正数向上；一格距离由 {@link #step} 决定。 */
	public void wheel(double amount) {
		target -= amount * step;
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		if (!shown() || !isMouseOver(mouseX, mouseY)) return false;
		wheel(vertical);
		return true;
	}
	@Override
	protected void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		update();
		if (!shown()) return;
		gui.blitSprite(TRACK, getX(), getY(), getWidth(), getHeight());
		var h = knobHeight();
		gui.blitSprite(KNOB, getX(), knobY(h), getWidth(), h);
	}
	/** 点滑块进入拖动（保留抓取偏移、不跳位）；点轨道只平滑跳到点击位置，不进入拖动。 */
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button != 0 || !shown() || !super.isMouseOver(mouseX, mouseY)) return false;
		var knobH = knobHeight();
		var knobY = knobY(knobH);
		if (mouseY >= knobY && mouseY < knobY + knobH) {
			dragging = true;
			grab = mouseY - knobYExact(knobH);
		} else {
			grab = knobH / 2.0;
			target = scrollFor(mouseY);
		}
		return true;
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (!dragging) return false;
		dragging = false;
		return true;
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (!dragging) return false;
		dragTo(mouseY);
		return true;
	}
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return shown() && (super.isMouseOver(mouseX, mouseY) || wheelArea.containsPoint((int) mouseX, (int) mouseY));
	}
	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput output) {}
	/** 直接落位、不插值：滑块按 {@code scroll} 绘制，走目标值会让鼠标移开后继续追赶。 */
	private void dragTo(double mouseY) {
		scroll = target = scrollFor(mouseY);
	}
	private double scrollFor(double mouseY) {
		var travel = getHeight() - knobHeight();
		if (travel <= 0) return scroll;
		var max = Math.max(0, owner.contentHeight() - getHeight());
		return Math.clamp((mouseY - grab - getY()) * (double) max / travel, 0, max);
	}
	/** 内容不超出一屏时整条不绘制也不响应事件。 */
	public boolean shown() {
		return owner.contentHeight() > getHeight();
	}
	private int knobHeight() {
		var content = owner.contentHeight();
		return content > getHeight() ? Math.max(MIN_KNOB_H, getHeight() * getHeight() / content) : 0;
	}
	private int knobY(int knobH) {
		return (int) knobYExact(knobH);
	}
	private double knobYExact(int knobH) {
		var max = Math.max(0, owner.contentHeight() - getHeight());
		return max > 0 ? getY() + (getHeight() - knobH) * (scroll / (double) max) : getY();
	}
}
