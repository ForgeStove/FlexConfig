package io.github.forgestove.flexconfig.client.gui.components;
import io.github.forgestove.flexconfig.client.gui.FlexGuiTextures;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
/**
 * 竖直滚动条组件：滚动量、平滑、滑块位置、命中、拖动、翻页、滚轮都在这里。
 * <p>
 * 持有者：{@link #area} 设几何 → 每帧 {@link #dragTo} + {@link #update} + {@link #render}，
 * 输入转 {@link #mousePressed} / {@link #release} / {@link #wheel}。
 * 若滚动量另有持有者（如原版列表的 {@code scrollAmount}），由它负责 {@link #seek} 同步进来、再推回去。
 */
public final class OreScrollbar {
	public static final int TRACK_COLOR = 0xFF58585A;
	public static final int TRACK_WIDTH = 2;
	/** 每 tick 走掉剩余差距的比例，越小越慢。 */
	private static final double DECAY_PER_TICK = 0.25;
	/** 残差小于此值就落位（px），免得永远差一点点。 */
	private static final double SNAP_PX = 0.5;
	private static final int MIN_KNOB_H = 12;
	/** 滑块下方的投影：高度（px）与颜色（半透明黑，罩在轨道上把它压暗）。 */
	private static final int SHADOW_H = 2, SHADOW_COLOR = 0x33000000;
	private double scroll, target, grab, step = 12;
	private boolean dragging;
	private long lastUpdate;
	/** 轨道几何：左缘、顶端、可视区高、内容总高。 */
	private int x, y, height, content;
	public void area(int x, int y, int height, int content) {
		this.x = x;
		this.y = y;
		this.height = height;
		this.content = content;
	}
	public void step(double step) {
		this.step = step;
	}
	/** 当前滚动量，渲染内容时按它平移。 */
	public double scroll() {
		return scroll;
	}
	/** 直接落位（恢复位置、跟随外部持有者）；超出部分由 {@link #update} 钳回。 */
	public void seek(double scroll) {
		this.scroll = target = scroll;
	}
	public void scrollBy(double delta) {
		target += delta;
	}
	/** {@code amount} 为滚轮格数，正数向上（与原版 {@code mouseScrolled} 的垂直量同号）。 */
	public void wheel(double amount) {
		target -= amount * step;
	}
	/** 按本帧经过的时间把位置推向目标，并钳到合法范围。 */
	public void update() {
		var now = Util.getMillis();
		var seconds = lastUpdate == 0 ? 0.0 : (now - lastUpdate) / 1000.0;
		lastUpdate = now;
		var max = maxScroll();
		target = Mth.clamp(target, 0, max);
		scroll = Mth.clamp(scroll, 0, max);
		// 纯比例衰减：离目标越远走得越快、接近时自然减速，且目标随时累加不会打断动画
		scroll = Mth.lerp(1 - Math.pow(DECAY_PER_TICK, seconds * 20), scroll, target);
		if (Math.abs(target - scroll) < SNAP_PX) scroll = target;
	}
	public boolean shown() {
		return content > height;
	}
	public void release() {
		dragging = false;
	}
	public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY) {
		if (!shown()) return;
		var thumbW = FlexGuiTextures.SCROLLBAR_TOP.width();
		var trackX = x + (thumbW - TRACK_WIDTH) / 2;
		gui.fill(trackX, y, trackX + TRACK_WIDTH, y + height, TRACK_COLOR);
		var knobH = knobHeight();
		var knobY = knobY(knobH);
		var top = FlexGuiTextures.SCROLLBAR_TOP.height();
		var bottom = FlexGuiTextures.SCROLLBAR_BOTTOM.height();
		FlexGuiTextures.SCROLLBAR_TOP.render(gui, x, knobY);
		FlexGuiTextures.SCROLLBAR_MID.render(gui, x, knobY + top, thumbW, Math.max(0, knobH - top - bottom));
		FlexGuiTextures.SCROLLBAR_BOTTOM.render(gui, x, knobY + knobH - bottom);
		// 滑块下缘的投影：按滑块宽度罩在轨道上，滑到底时被轨道下界截断
		var shadowY = knobY + knobH;
		if (shadowY < y + height) gui.fill(x, shadowY, x + thumbW, Math.min(shadowY + SHADOW_H, y + height), SHADOW_COLOR);
		if (mouseX >= x && mouseX < x + thumbW && mouseY >= y && mouseY < y + height) OreCursor.hand();
	}
	/** 点在滑块上为抓取拖动，点在轨道空白处为翻页。鼠标不在滚动条上时返回 false。 */
	public boolean mousePressed(double mouseX, double mouseY) {
		if (!shown() || mouseX < x || mouseX >= x + FlexGuiTextures.SCROLLBAR_TOP.width()) return false;
		var knobH = knobHeight();
		var knobY = knobY(knobH);
		if (mouseY >= knobY && mouseY < knobY + knobH) {
			dragging = true;
			grab = mouseY - knobYExact(knobH);
		} else scrollBy(mouseY < knobY ? -height : height);
		return true;
	}
	/**
	 * 拖动落位，按抓取时的偏移还原位置。
	 * <p>
	 * 每帧在渲染里喂当前鼠标坐标即可（不要接 {@code mouseDragged}）；拖动直接落位、不走平滑插值，
	 * 否则鼠标移开后滑块仍会追赶、表现为不跟手。
	 */
	public void dragTo(double mouseY) {
		if (!dragging || !shown()) return;
		var travel = height - knobHeight();
		if (travel <= 0) return;
		var max = maxScroll();
		scroll = target = Mth.clamp((mouseY - grab - y) * (double) max / travel, 0, max);
	}
	private int maxScroll() {
		return Math.max(0, content - height);
	}
	private int knobHeight() {
		return shown() ? Math.max(MIN_KNOB_H, height * height / content) : 0;
	}
	private int knobY(int knobH) {
		return (int) knobYExact(knobH);
	}
	/**
	 * 未取整的滑块顶端位置。
	 * <p>
	 * 拖动时 {@code grab} 必须用它来算（不能用取整后的 {@link #knobY}）：{@link #dragTo} 的反函数是精确的，
	 * 而取整会丢掉小数，于是每按一下滚动量就回落一点点 —— 表现为原地连点滑块一直往上爬。
	 */
	private double knobYExact(int knobH) {
		var max = maxScroll();
		return max > 0 ? y + (height - knobH) * (scroll / (double) max) : y;
	}
}
