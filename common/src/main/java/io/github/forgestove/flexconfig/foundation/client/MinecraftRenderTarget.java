package io.github.forgestove.flexconfig.foundation.client;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
/**
 * UiRenderTarget 的 Minecraft 实现，把绘制调用转发给 GuiGraphics，并把 UiText 和 UiTexture
 * 还原成 Component、ResourceLocation。
 */
public final class MinecraftRenderTarget implements UiRenderTarget {
	private final GuiGraphics gui;
	public MinecraftRenderTarget(GuiGraphics gui) {
		this.gui = gui;
	}
	@Override
	public void pushPose() {
		gui.pose().pushPose();
	}
	@Override
	public void popPose() {
		gui.pose().popPose();
	}
	@Override
	public void translate(float x, float y, float z) {
		gui.pose().translate(x, y, z);
	}
	@Override
	public void scale(float x, float y, float z) {
		gui.pose().scale(x, y, z);
	}
	@Override
	public void fill(int x1, int y1, int x2, int y2, int color) {
		gui.fill(x1, y1, x2, y2, color);
	}
	@Override
	public void fillGradient(int x1, int y1, int x2, int y2, int top, int bottom) {
		gui.fillGradient(x1, y1, x2, y2, top, bottom);
	}
	@Override
	public void blit(
		UiTexture texture,
		int x,
		int y,
		int width,
		int height,
		float u,
		float v,
		int uWidth,
		int vHeight,
		int texWidth,
		int texHeight
	) {
		if (!(texture.handle() instanceof ResourceLocation id)) return;
		gui.blit(id, x, y, width, height, u, v, uWidth, vHeight, texWidth, texHeight);
	}
	@Override
	public void text(UiText text, int x, int y, int color, boolean shadow) {
		var renderable = MinecraftTextEngine.toRenderable(text);
		if (renderable instanceof Component component) gui.drawString(ClientUtil.mc.font, component, x, y, color, shadow);
		else if (renderable instanceof FormattedCharSequence sequence) gui.drawString(ClientUtil.mc.font, sequence, x, y, color, shadow);
	}
	@Override
	public void enableScissor(int x1, int y1, int x2, int y2) {
		gui.enableScissor(x1, y1, x2, y2);
	}
	@Override
	public void disableScissor() {
		gui.disableScissor();
	}
	@Override
	public void flush() {
		gui.flush();
	}
	@Override
	public void setShaderColor(float red, float green, float blue, float alpha) {
		RenderSystem.setShaderColor(red, green, blue, alpha);
	}
}
