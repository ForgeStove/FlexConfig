package io.github.forgestove.flexconfig.client.gui.components;
import io.github.forgestove.flexconfig.client.gui.FlexGuiTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.sounds.SoundManager;
import org.jetbrains.annotations.NotNull;
/**
 * tab 按钮：方盒 + 选中态白色下划线 + 聚焦时零间隙白环。
 * 盒子横向均匀、纵向才分层，所以用三段切拼（左右端帽贴齐 + 中段横向拉伸），配色全在纹理里。
 * 文字沿用 {@link AbstractButton} 的渲染 —— 禁用只改文字、不改盒子。
 */
public final class OreTabButton extends OreAbstractButton {
	private record Box(FlexGuiTextures left, FlexGuiTextures mid, FlexGuiTextures right) {}
	// 只有 3 种外观：ON（选中全状态 + 未选中按下）、OFF、OFF_HOVER；选中的差别只多一条下划线
	private static final Box ON = new Box(FlexGuiTextures.TAB_ON_L, FlexGuiTextures.TAB_ON_M, FlexGuiTextures.TAB_ON_R);
	private static final Box OFF = new Box(FlexGuiTextures.TAB_OFF_L, FlexGuiTextures.TAB_OFF_M, FlexGuiTextures.TAB_OFF_R);
	private static final Box OFF_HOVER = new Box(
		FlexGuiTextures.TAB_OFF_HOVER_L,
		FlexGuiTextures.TAB_OFF_HOVER_M,
		FlexGuiTextures.TAB_OFF_HOVER_R
	);
	private static final int RING_INSET = 1, RING_COLOR = 0xFFFFFFFF;
	/** 文字左右留白（同原版 {@code AbstractButton} 的滚动文案边距）。 */
	private static final int TEXT_MARGIN = 2;
	/** 文字内容区相对盒顶的上界（让开描边），以及未选中盒底部那段空档的高度（与生成器的 {@code TAB_BAND} 同值）。 */
	private static final int TEXT_TOP = 1, TEXT_BAND = 2;
	private static final int UNDERLINE_H = 1, UNDERLINE_COLOR = 0xFFFFFFFF;
	private final TabManager tabManager;
	private final Tab tab;
	public OreTabButton(TabManager tabManager, Tab tab, int width, int height) {
		super(0, 0, width, height, tab.getTabTitle());
		this.tabManager = tabManager;
		this.tab = tab;
	}
	public Tab tab() {
		return tab;
	}
	public boolean isSelected() {
		return tabManager.getCurrentTab() == tab;
	}
	public static int preferredHeight() {
		return FlexGuiTextures.TAB_OFF_L.height();
	}
	@Override
	protected void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		var box = box();
		var capW = box.left().width();
		var height = box.left().height();
		var y = getY() + preferredHeight() - height;   // 未选中的比选中的高，两者底部对齐
		box.left().render(gui, getX(), y);
		box.mid().render(gui, getX() + capW, y, getWidth() - 2 * capW, height);
		box.right().render(gui, getX() + getWidth() - capW, y);
		if (isSelected()) renderUnderline(gui, y, height);
		// 文字居中于盒子的**填充区**：让开上描边，以及未选中盒底部那段空档（选中盒没有）
		renderTabString(gui, y + TEXT_TOP, y + height - 1 - (box == ON ? 0 : TEXT_BAND));
		if (isFocused()) renderRing(gui, y, height);
		// 选中的 tab 视觉上就是"按下"状态，点它不会切到别处，指针保持普通箭头
		if (active && isHoveredOrFocused() && !isSelected()) OreCursor.hand();
	}
	private void renderTabString(@NotNull GuiGraphics gui, int top, int bottom) {
		renderScrollingString(
			gui,
			Minecraft.getInstance().font,
			getMessage(),
			getX() + TEXT_MARGIN,
			top,
			getX() + getWidth() - TEXT_MARGIN,
			bottom,
			active ? 0xFFFFFFFF : 0xFFA0A0A0
		);
	}
	/** 按下与选中共用同一套盒子（ON）；按下要排在聚焦之前，否则点一下拿到焦点后按下态会被盖掉。 */
	private Box box() {
		var on = isSelected();
		if (!active) return on ? ON : OFF;
		if (on || isPressed()) return ON;
		if (isFocused()) return OFF;
		return isHovered() ? OFF_HOVER : OFF;
	}
	private void renderUnderline(@NotNull GuiGraphics gui, int y, int height) {
		var width = Math.min(Minecraft.getInstance().font.width(getMessage()), getWidth() - 4);
		var x = getX() + (getWidth() - width) / 2;
		gui.fill(x, y + height - 1 - UNDERLINE_H, x + width, y + height - 1, UNDERLINE_COLOR);
	}
	private void renderRing(@NotNull GuiGraphics gui, int y, int height) {
		gui.renderOutline(getX() - RING_INSET, y - RING_INSET, getWidth() + 2 * RING_INSET, height + 2 * RING_INSET, RING_COLOR);
	}
	/** 切换由栏在松手时调 {@code setCurrentTab} 完成，这里无需动作。 */
	@Override
	public void onPress() {}
	/** 点击音由 {@link TabManager#setCurrentTab} 播。 */
	@Override
	public void playDownSound(@NotNull SoundManager soundManager) {}
}
