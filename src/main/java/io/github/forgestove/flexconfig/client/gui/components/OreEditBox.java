package io.github.forgestove.flexconfig.client.gui.components;
import io.github.forgestove.flexconfig.client.gui.FlexGuiTextures;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;
/**
 * 每状态一张完整贴图加九宫格元数据，纵向也切片故高度可拉伸。原版背景靠重写 {@link #isBordered()} 关掉：
 * 背景判断走方法而文字排版走字段，故 {@code bordered} 字段仍保持 true。
 */
public class OreEditBox extends EditBox {
	private static final FlexGuiTextures NORMAL = FlexGuiTextures.FIELD_NORMAL;
	private static final FlexGuiTextures DISABLED = FlexGuiTextures.FIELD_DISABLED;
	private static final int TEXT_DISABLED = 0xFF48494A;
	private static final int RING_GAP = 0, RING_W = 1, RING = 0xFFFFFFFF;
	@Nullable private static OreEditBox focusedInstance;
	public OreEditBox(Font font, int x, int y, int width, int height, Component message) {
		super(font, x, y, width, height, message);
		setTextColor(0xFFFFFFFF);
		setTextColorUneditable(TEXT_DISABLED);
	}
	public static void onScreenMouseClicked(double mouseX, double mouseY) {
		if (focusedInstance == null) return;
		if (!focusedInstance.isMouseOver(mouseX, mouseY)) focusedInstance.setFocused(false);
	}
	@Override
	public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		renderOreBackground(gui);
		super.renderWidget(gui, mouseX, mouseY, partialTick);
		if (active && isMouseOver(mouseX, mouseY)) OreCursor.iBeam();
	}
	/** 关掉原版背景（自绘），但 {@code bordered} 字段保持 true 以维持文字排版。 */
	@Override
	public boolean isBordered() {
		return false;
	}
	@Override
	public void setFocused(boolean focused) {
		if (focused && focusedInstance != this) {
			if (focusedInstance != null) focusedInstance.setFocused(false);
			focusedInstance = this;
		} else if (!focused && focusedInstance == this) focusedInstance = null;
		super.setFocused(focused);
	}
	/** 内宽也走 {@link #isBordered()}，恒 false 会被算成整个宽度。 */
	@Override
	public int getInnerWidth() {
		return getWidth() - 8;
	}
	private void renderOreBackground(@NotNull GuiGraphics gui) {
		(active ? NORMAL : DISABLED).render(gui, getX(), getY(), getWidth(), getHeight());
		if (isFocused()) {
			var inset = RING_GAP + RING_W;
			gui.renderOutline(getX() - inset, getY() - inset, getWidth() + 2 * inset, getHeight() + 2 * inset, RING);
		}
	}
}
