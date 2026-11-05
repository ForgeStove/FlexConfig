package io.github.forgestove.flexconfig.client.ui.core;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.forgestove.flexconfig.client.ClientUtil;
import io.github.forgestove.flexconfig.client.ui.FlexGuiTextures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.*;
/** 唯一绘制入口：新界面只允许通过这里画东西，跨版本差异和渲染顺序集中管理。 */
public class UiPainter {
	private final GuiGraphics gui;
	private final ArrayDeque<Float> alphaStack = new ArrayDeque<>();
	private float alpha = 1F;
	private float scale = 1F;
	private int scaleDepth;
	public UiPainter(@NotNull GuiGraphics gui) {
		this.gui = gui;
	}
	public void pushAlpha(float factor) {
		alphaStack.push(alpha);
		alpha *= UiMath.clamp(factor, 0F, 1F);
	}
	public void popAlpha() {
		if (!alphaStack.isEmpty()) alpha = alphaStack.pop();
	}
	private int col(int argb) {
		var mapped = UiPrefs.highContrastColor(argb);
		var a = Math.round((mapped >>> 24 & 0xFF) * alpha);
		return a << 24 | mapped & 0xFFFFFF;
	}
	public void pushScale(float factor) {
		flush();
		gui.pose().pushPose();
		if (factor != 1F) {
			gui.pose().scale(factor, factor, 1F);
			scale *= factor;
		}
		scaleDepth++;
	}
	public void popScale() {
		if (scaleDepth == 0) return;
		flush();
		gui.pose().popPose();
		scaleDepth--;
		if (scaleDepth == 0) scale = 1F;
	}
	public void pushTranslate(float x, float y) {
		flush();
		gui.pose().pushPose();
		gui.pose().translate(x, y, 0);
	}
	public void popTranslate() {
		flush();
		gui.pose().popPose();
	}
	public void rect(UiBounds bounds, int color) {
		rect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), color);
	}
	public void rect(int x, int y, int width, int height, int color) {
		if (width <= 0 || height <= 0 || alpha <= 0.004F) return;
		gui.fill(x, y, x + width, y + height, col(color));
	}
	private static final Map<ResourceLocation, int[]> TEXTURE_SIZES = new HashMap<>();
	public void texture(ResourceLocation texture, UiBounds bounds) {
		if (texture == null || bounds.width() <= 0 || bounds.height() <= 0 || alpha <= 0.004F) return;
		var size = TEXTURE_SIZES.computeIfAbsent(texture, id -> {
			var optional = ClientUtil.mc.getResourceManager().getResource(id);
			if (optional.isEmpty()) return null;
			try (var image = NativeImage.read(optional.get().open())) {
				return new int[]{image.getWidth(), image.getHeight()};
			} catch (Exception e) {
				return null;
			}
		});
		if (size == null) return;
		gui.blit(texture, bounds.x(), bounds.y(), 0F, 0F, bounds.width(), bounds.height(), size[0], size[1]);
	}
	public void gradient(UiBounds bounds, int top, int bottom) {
		if (bounds.width() <= 0 || bounds.height() <= 0 || alpha <= 0.004F) return;
		gui.fillGradient(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(), col(top), col(bottom));
	}
	public void border(UiBounds bounds, int color) {
		border(bounds.x(), bounds.y(), bounds.width(), bounds.height(), color);
	}
	public void border(int x, int y, int width, int height, int color) {
		if (width <= 0 || height <= 0) return;
		rect(x, y, width, 1, color);
		rect(x, y + height - 1, width, 1, color);
		rect(x, y + 1, 1, height - 2, color);
		rect(x + width - 1, y + 1, 1, height - 2, color);
	}
	/** 立体边：外描边、顶部/左右高光、底部亮线、面和底部厚边；返回面矩形，按下时整体下沉。 */
	public UiBounds bevel(
		UiBounds bounds,
		int outline,
		int face,
		int highlight,
		int shine,
		int depthColor,
		int depth,
		boolean pressed
	) {
		var top = pressed ? bounds.y() + depth : bounds.y();
		border(bounds.x(), top, bounds.width(), bounds.bottom() - top, outline);
		var fx0 = bounds.x() + 1;
		var fx1 = bounds.right() - 1;
		var fy0 = top + 1;
		var fy1 = bounds.bottom() - 1 - (pressed ? 0 : depth);
		if (fx1 - fx0 >= 2 && fy1 - fy0 >= 2) {
			rect(fx0, fy0, fx1 - fx0, 1, highlight);
			rect(fx0, fy0 + 1, 1, fy1 - fy0 - 2, highlight);
			rect(fx1 - 1, fy0 + 1, 1, fy1 - fy0 - 2, highlight);
			rect(fx0, fy1 - 1, fx1 - fx0, 1, pressed ? UiTheme.fadeToward(shine, face, 0.55F) : shine);
			rect(fx0 + 1, fy0 + 1, fx1 - fx0 - 2, fy1 - fy0 - 2, face);
			if (!pressed) rect(fx0, fy1, fx1 - fx0, bounds.bottom() - 1 - fy1, depthColor);
		} else rect(fx0, fy0, fx1 - fx0, bounds.bottom() - 1 - fy0, face);
		return new UiBounds(fx0, fy0, Math.max(0, fx1 - fx0), Math.max(0, fy1 - fy0));
	}
	public void groove(int x1, int x2, int y) {
		rect(x1, y, x2 - x1, 1, UiTheme.SEP);
		rect(x1, y + 1, x2 - x1, 1, UiTheme.GROOVE_LIGHT);
	}
	public void field(UiBounds bounds, boolean focused, boolean hovered, int focusColor) {
		var border = focused ? focusColor : hovered ? 0xFFC8C8CE : UiTheme.WINDOW_EDGE;
		border(bounds, border);
		var inner = bounds.inset(1, 1);
		if (inner.width() <= 0 || inner.height() <= 0) return;
		rect(inner.x(), inner.y(), inner.width(), 1, UiTheme.FIELD_SHADOW);
		rect(inner.x(), inner.y() + 1, 1, inner.height() - 1, UiTheme.FIELD_SHADOW);
		rect(inner.x() + 1, inner.y() + 1, Math.max(0, inner.width() - 1), Math.max(0, inner.height() - 1), UiTheme.FIELD);
	}
	public void disc(int x, int y, int diameter, int argb) {
		var r = diameter / 2.0;
		var runStart = 0;
		var runLeft = -1;
		var runRight = -1;
		for (var row = 0; row <= diameter; row++) {
			var left = -1;
			var right = -1;
			if (row < diameter) {
				var dy = row + 0.5 - r;
				var half = Math.sqrt(Math.max(0, r * r - dy * dy));
				left = (int) Math.ceil(r - 0.5 - half - 1.0E-9);
				right = (int) Math.floor(r - 0.5 + half + 1.0E-9) + 1;
			}
			if (left != runLeft || right != runRight) {
				if (runRight > runLeft) rect(x + runLeft, y + runStart, runRight - runLeft, row - runStart, argb);
				runStart = row;
				runLeft = left;
				runRight = right;
			}
		}
	}
	/** 7×4 的下拉箭头，像素画法。 */
	public void chevron(int x, int y, int argb) {
		for (var i = 0; i < 4; i++) {
			rect(x + i, y + i, 1, 1, argb);
			rect(x + 6 - i, y + i, 1, 1, argb);
		}
	}
	public void text(String text, int x, int y, int color) {
		text(text, x, y, color, false);
	}
	public void text(String text, int x, int y, int color, boolean shadow) {
		if (text == null || text.isEmpty() || alpha <= 0.004F) return;
		gui.drawString(ClientUtil.mc.font, text, x, y, col(color), shadow);
	}
	public void text(Component text, int x, int y, int color) {
		text(text, x, y, color, false);
	}
	public void text(Component text, int x, int y, int color, boolean shadow) {
		if (alpha <= 0.004F) return;
		gui.drawString(ClientUtil.mc.font, text, x, y, col(color), shadow);
	}
	public void text(FormattedCharSequence text, int x, int y, int color) {
		if (alpha <= 0.004F) return;
		gui.drawString(ClientUtil.mc.font, text, x, y, col(color), false);
	}
	public void textEmphasis(String text, int x, int y, int color) {
		text(Component.literal(text).withStyle(ChatFormatting.BOLD), x, y, color, false);
	}
	public void textEmphasis(Component text, int x, int y, int color) {
		text(text.copy().withStyle(ChatFormatting.BOLD), x, y, color, false);
	}
	public int textWidthEmphasis(String text) {
		return textWidth(Component.literal(text).withStyle(ChatFormatting.BOLD));
	}
	public void textRight(Component text, int right, int y, int color) {
		text(text, right - textWidth(text), y, color);
	}
	public int textWidth(String text) {
		return ClientUtil.mc.font.width(text);
	}
	public int textWidth(Component text) {
		return ClientUtil.mc.font.width(text);
	}
	public int lineHeight() {
		return ClientUtil.mc.font.lineHeight;
	}
	public String trim(String text, int maxWidth) {
		return ClientUtil.mc.font.plainSubstrByWidth(text, maxWidth);
	}
	/** 图标统一走 {@link FlexGuiTextures}；染色通过 shader color，由本方法负责复位。 */
	public void icon(FlexGuiTextures icon, int x, int y, int argb) {
		if (icon == null || alpha <= 0.004F) return;
		var color = col(argb);
		RenderSystem.setShaderColor(
			(color >> 16 & 0xFF) / 255F,
			(color >> 8 & 0xFF) / 255F,
			(color & 0xFF) / 255F,
			(color >>> 24 & 0xFF) / 255F
		);
		icon.render(gui, x, y);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
	}
	public void iconCentered(FlexGuiTextures icon, UiBounds bounds, int argb) {
		if (icon == null) return;
		icon(icon, bounds.x() + (bounds.width() - icon.width()) / 2, bounds.y() + (bounds.height() - icon.height()) / 2, argb);
	}
	public void scissorOn(UiBounds bounds) {
		flush();
		gui.enableScissor(
			Math.round(bounds.x() * scale),
			Math.round(bounds.y() * scale),
			Math.round(bounds.right() * scale),
			Math.round(bounds.bottom() * scale)
		);
	}
	public void scissorOff() {
		flush();
		gui.disableScissor();
	}
	public void pushZ(float z) {
		flush();
		gui.pose().pushPose();
		gui.pose().translate(0, 0, z);
	}
	public void popZ() {
		flush();
		gui.pose().popPose();
	}
	public void flush() {
		gui.flush();
	}
}
