package io.github.forgestove.flexconfig.client.gui.components;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.forgestove.flexconfig.client.gui.FlexGuiTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
public class FlexIconButton extends Button {
	/** 禁用态按原版文字亮度（{@code 0xA0A0A0} / {@code 0xFFFFFF}）压暗图标。 */
	private static final int DISABLED = 0xA0;
	private FlexGuiTextures icon;
	/** 图标的 ARGB 颜色；为 null 时保持贴图原色。 */
	@Nullable private Integer iconColor;
	public FlexIconButton(int width, int height, @NotNull FlexGuiTextures icon, @NotNull OnPress onPress) {
		super(0, 0, width, height, Component.empty(), onPress, DEFAULT_NARRATION);
		this.icon = icon;
	}
	public void setIcon(@NotNull FlexGuiTextures icon) {
		this.icon = icon;
	}
	public void setIconColor(@Nullable Integer iconColor) {
		this.iconColor = iconColor;
	}
	@Override
	protected void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		super.renderWidget(gui, mouseX, mouseY, partialTick);
		var x = getX() + (getWidth() - icon.width()) / 2;
		var y = getY() + (getHeight() - icon.height()) / 2;
		if (iconColor == null) {
			renderIcon(gui, x, y, active ? 0xFFFFFFFF : dim(0xFFFFFFFF));
			return;
		}
		var color = active ? iconColor : dim(iconColor);
		// 阴影：与文字相同的 (color & 0xFCFCFC) >> 2，偏移 1px
		renderIcon(gui, x + 1, y + 1, (color & 0xFCFCFC) >> 2);
		renderIcon(gui, x, y, color);
	}
	private void renderIcon(@NotNull GuiGraphics gui, int x, int y, int argb) {
		RenderSystem.setShaderColor((argb >> 16 & 0xFF) / 255F, (argb >> 8 & 0xFF) / 255F, (argb & 0xFF) / 255F, 1F);
		icon.render(gui, x, y);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
	}
	private static int dim(int argb) {
		var r = (argb >> 16 & 0xFF) * DISABLED / 0xFF;
		var g = (argb >> 8 & 0xFF) * DISABLED / 0xFF;
		var b = (argb & 0xFF) * DISABLED / 0xFF;
		return 0xFF000000 | r << 16 | g << 8 | b;
	}
}
