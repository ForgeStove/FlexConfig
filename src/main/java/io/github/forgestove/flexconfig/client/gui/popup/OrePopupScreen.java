package io.github.forgestove.flexconfig.client.gui.popup;
import io.github.forgestove.flexconfig.client.ClientUtil;
import io.github.forgestove.flexconfig.client.gui.components.OreCursor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.*;
/** 独立 {@link Screen}，父界面不必转发事件；渲染顺序为父界面（z −100）→ 背景 → 面板 → 内容 → 控件。 */
public abstract class OrePopupScreen extends Screen {
	/** Ore 面板配色：正文本底 / 描边 / 带子底 / 上·左亮边 / 下·右暗边（都是纯色，直接画、无需纹理）。 */
	protected static final int SURFACE = 0xFF313233, BORDER = 0xFF1E1E1F, BAND = 0xFF48494A;
	protected static final int LIGHT = 0xFF6B6B6C, DARK = 0xFF5A5B5C;
	/** 标题与 ✕ 的文字/线条色。 */
	protected static final int TEXT_COLOR = 0xFFFFFFFF;
	/** 面板内边距、标题栏高度、底栏高度。 */
	protected static final int PAD = 3, HEADER_H = 19, FOOTER_H = 54;
	/** ✕ 的边长与右留白；命中与高亮框的边长另见 {@link #closeSize()}。 */
	private static final int CLOSE_SIZE = 7, CLOSE_MARGIN = 6;
	private static final int CLOSE_HOVER = 0xFF56585B;
	protected final Screen parent;
	@Nullable private final Component title;
	protected int panelX, panelY, panelWidth, panelHeight;
	/** 触发控件矩形；为 null 表示居中模态。 */
	@Nullable private Rect anchor;
	protected OrePopupScreen(@NotNull Screen parent, @Nullable Component title) {
		super(CommonComponents.EMPTY);
		this.parent = parent;
		this.title = title;
	}
	public void anchorTo(int x, int y, int width, int height) {
		anchor = new Rect(x, y, width, height);
	}
	protected void useCenter() {
		anchor = null;
	}
	protected void renderContent(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {}
	protected final int contentX() {
		return panelX + PAD;
	}
	protected final int contentY() {
		return panelY + PAD + headerHeight();
	}
	protected final int contentY(int blockHeight) {
		return contentY() + Math.max(0, (contentH() - blockHeight) / 2);
	}
	protected final int headerHeight() {
		return title == null ? 0 : HEADER_H;
	}
	protected final int contentW() {
		return panelWidth - PAD * 2;
	}
	protected final int contentH() {
		return panelHeight - PAD * 2 - headerHeight() - footerHeight();
	}
	protected final int footerHeight() {
		return hasFooter() ? FOOTER_H : 0;
	}
	/** 底栏是放按钮的实色带，默认不画。 */
	protected boolean hasFooter() {
		return false;
	}
	/** 重算面板矩形并摆放内容；{@link #layoutContent()} 须等尺寸定下后才可读 panelWidth 等字段。 */
	protected final void relayout() {
		// 面板至少要跟锚定控件一样宽：下拉挂在按钮正下方，宽度不齐看着就错位
		panelWidth = Math.min(contentWidth() + PAD * 2, width);
		if (anchor != null) panelWidth = Math.clamp(anchor.width(), panelWidth, width);
		panelHeight = Math.min(contentHeight() + PAD * 2 + headerHeight() + footerHeight(), height);
		if (anchor == null) {
			panelX = (width - panelWidth) / 2;
			panelY = (height - panelHeight) / 2;
		} else {
			panelX = Mth.clamp(anchorCenterX() - panelWidth / 2, 0, Math.max(0, width - panelWidth));
			var below = anchorBottom();
			panelY = below + panelHeight <= height ? below : Math.max(0, anchor.y() - panelHeight);
		}
		layoutContent();
	}
	protected void buildWidgets() {}
	/** 内容区尺寸，不含内边距与标题栏。 */
	protected abstract int contentWidth();
	protected abstract int contentHeight();
	protected final int anchorCenterX() {
		return anchor == null ? width / 2 : anchor.x() + anchor.width() / 2;
	}
	/** 无锚点时为 -1，调用方据此区分居中模态。 */
	protected final int anchorBottom() {
		return anchor == null ? -1 : anchor.y() + anchor.height();
	}
	/** 锚点下方能给的净空间（不含面板内边距）；无锚点时为全屏。 */
	protected final int spaceBelow() {
		return anchor == null ? height - PAD * 2 : height - anchorBottom() - PAD * 2;
	}
	/** 锚点上方能给的净空间；无锚点时为全屏。 */
	protected final int spaceAbove() {
		return anchor == null ? height - PAD * 2 : anchor.y() - PAD * 2;
	}
	protected void layoutContent() {}
	@Override
	public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		renderParent(gui, partialTick);
		renderBackdrop(gui, mouseX, mouseY, partialTick);
		renderPanel(gui, mouseX, mouseY);
		renderContent(gui, mouseX, mouseY, partialTick);
		if (title != null && isOverClose(mouseX, mouseY)) OreCursor.hand();
		super.render(gui, mouseX, mouseY, partialTick);
	}
	/** 关闭即回到父界面，不走 {@code Screen} 的弹出界面栈。 */
	@Override
	public void onClose() {
		ClientUtil.mc.setScreen(parent);
	}
	@Override
	protected void init() {
		super.init();
		relayout();       // 先定面板矩形，子控件按它创建（如搜索框宽度）
		buildWidgets();
		relayout();       // 控件就位后再摆一遍
	}
	@Override
	public void renderBackground(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {}
	/** 窗口尺寸变化只走 {@code repositionElements}（见 {@link Screen#resize}），故在此重算。 */
	@Override
	protected void repositionElements() {
		relayout();
	}
	@Override
	public void resize(@NotNull Minecraft minecraft, int width, int height) {
		super.resize(minecraft, width, height);
		parent.resize(minecraft, width, height);
		relayout();
	}
	protected void renderBackdrop(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {}
	/** 父界面画在下方：压到 z −100，并临时把 {@code mc.screen} 指回父界面（父界面渲染里可能读它）。 */
	private void renderParent(@NotNull GuiGraphics gui, float partialTick) {
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(0, 0, -100);
		var mc = ClientUtil.mc;
		var previous = mc.screen;
		mc.screen = parent;
		try {
			parent.render(gui, -1, -1, partialTick);
		} finally {
			// 父界面渲染若抛异常，mc.screen 会永久停在父界面上
			mc.screen = previous;
		}
		pose.popPose();
	}
	private void renderPanel(@NotNull GuiGraphics gui, int mouseX, int mouseY) {
		drawPanel(gui);
		if (title != null) renderHeader(gui, title, mouseX, mouseY);
		if (footerHeight() > 0) renderFooter(gui);
	}
	/** 面板底：正文本底 + 1px 描边。 */
	protected void drawPanel(@NotNull GuiGraphics gui) {
		gui.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, SURFACE);
		gui.renderOutline(panelX, panelY, panelWidth, panelHeight, BORDER);
	}
	/** 标题栏：底色 + 上·左亮边 + 下·右暗边 + 居中粗体标题 + 右侧关闭 ✕（悬停垫一层高亮）。 */
	private void renderHeader(@NotNull GuiGraphics gui, @NotNull Component title, int mouseX, int mouseY) {
		var x = panelX + 1;        // 内缩 1px：别盖住面板那圈描边
		var y = panelY + 1;
		var w = panelWidth - 2;
		var bottom = y + HEADER_H;
		gui.fill(x, y, x + w, bottom, BAND);
		gui.fill(x, y, x + w, y + 1, LIGHT);                        // 上亮边
		gui.fill(x, y + 1, x + 1, bottom - 1, LIGHT);               // 左亮边
		gui.fill(x, bottom - 1, x + w, bottom, DARK);               // 下暗边
		gui.fill(x + w - 1, y + 1, x + w, bottom - 1, DARK);        // 右暗边
		var text = title.copy().withStyle(ChatFormatting.BOLD);
		gui.drawString(font, text.getVisualOrderText(), x + w / 2 - font.width(text) / 2, y + (HEADER_H - 8) / 2, TEXT_COLOR, true);
		if (isOverClose(mouseX, mouseY))
			gui.fill(closeLeft(), closeTop(), closeLeft() + closeSize(), closeTop() + closeSize(), CLOSE_HOVER);
		drawClose(gui);
	}
	/** 底栏：底色 + 上缘亮线。 */
	private void renderFooter(@NotNull GuiGraphics gui) {
		var x = panelX + 1;        // 内缩 1px：别盖住面板那圈描边
		var w = panelWidth - 2;
		var y = panelY + panelHeight - 1 - FOOTER_H;
		gui.fill(x, y, x + w, y + FOOTER_H, BAND);
		gui.fill(x, y, x + w, y + 1, DARK);
	}
	/** 关闭 ✕：两条 1px 对角线。 */
	private void drawClose(@NotNull GuiGraphics gui) {
		var x = closeX();
		var y = closeY();
		for (var i = 0; i < CLOSE_SIZE; i++) {
			gui.fill(x + i, y + i, x + i + 1, y + i + 1, TEXT_COLOR);
			gui.fill(x + CLOSE_SIZE - 1 - i, y + i, x + CLOSE_SIZE - i, y + i + 1, TEXT_COLOR);
		}
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (isOutsidePanel(mouseX, mouseY) || title != null && isOverClose(mouseX, mouseY)) {
			onClose();
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		if (isOutsidePanel(mouseX, mouseY)) return true;
		return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
	}
	protected final boolean isOutsidePanel(double mouseX, double mouseY) {
		return mouseX < panelX || mouseX >= panelX + panelWidth || mouseY < panelY || mouseY >= panelY + panelHeight;
	}
	private boolean isOverClose(double mouseX, double mouseY) {
		return mouseX >= closeLeft() && mouseX < closeLeft() + closeSize() && mouseY >= closeTop() && mouseY < closeTop() + closeSize();
	}
	private int closeLeft() {
		return closeX() + (CLOSE_SIZE - closeSize()) / 2;
	}
	private int closeSize() {
		return HEADER_H - 4;
	}
	private int closeTop() {
		return closeY() + (CLOSE_SIZE - closeSize()) / 2;
	}
	private int closeX() {
		return panelX + panelWidth - 1 - CLOSE_MARGIN - CLOSE_SIZE;
	}
	/** 内缩 1px 避开面板描边，上下余量各一半。 */
	private int closeY() {
		return panelY + 1 + (HEADER_H - CLOSE_SIZE) / 2;
	}
	/** 锚点矩形（避免为它依赖 {@code ScreenRectangle}）。 */
	protected record Rect(int x, int y, int width, int height) {}
}
