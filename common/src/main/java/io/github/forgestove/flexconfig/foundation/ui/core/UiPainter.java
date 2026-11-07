package io.github.forgestove.flexconfig.foundation.ui.core;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.foundation.client.ClientUtil;
import io.github.forgestove.flexconfig.foundation.ui.FlexGuiTextures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.*;
/** 所有界面都通过这里绘制，跨版本差异和渲染顺序也在这里处理。 */
public class UiPainter {
	private static final Logger LOGGER = LogUtils.getLogger();
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
		alpha *= Mth.clamp(factor, 0F, 1F);
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
	private static final Map<ResourceLocation, Optional<int[]>> TEXTURE_SIZES = new HashMap<>();
	public void texture(ResourceLocation texture, UiBounds bounds) {
		if (texture == null || bounds.width() <= 0 || bounds.height() <= 0 || alpha <= 0.004F) return;
		var size = TEXTURE_SIZES.computeIfAbsent(
			texture, id -> {
				var resource = ClientUtil.mc.getResourceManager().getResource(id);
				if (resource.isEmpty()) {
					LOGGER.warn("Missing texture {}", id);
					return Optional.empty();
				}
				try (var image = NativeImage.read(resource.get().open())) {
					return Optional.of(new int[]{image.getWidth(), image.getHeight()});
				} catch (Exception e) {
					LOGGER.warn("Failed to read texture {}", id, e);
					return Optional.empty();
				}
			}
		);
		if (size.isEmpty()) return;
		var dims = size.get();
		gui.blit(texture, bounds.x(), bounds.y(), bounds.width(), bounds.height(), 0F, 0F, dims[0], dims[1], dims[0], dims[1]);
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
	public UiBounds bevel(UiBounds bounds, int outline, int face, int highlight, int shine, int depthColor, int depth, boolean pressed) {
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
	/** 凹槽背景：1px 描边、深色底和顶部内阴影，输入框与按键槽共用；light 是悬停提亮比例。 */
	public void well(UiBounds bounds, int borderColor, float light) {
		rect(bounds, borderColor);
		rect(
			bounds.x() + 1,
			bounds.y() + 1,
			bounds.width() - 2,
			bounds.height() - 2,
			UiTheme.fadeToward(UiTheme.PANEL, UiTheme.SIDEBAR, light)
		);
		rect(bounds.x() + 1, bounds.y() + 1, bounds.width() - 2, 1, 0xFF242425);
	}
	/** 7×4 的下拉箭头，像素画法。 */
	public void chevron(int x, int y, int argb) {
		for (var i = 0; i < 4; i++) {
			rect(x + i, y + i, 1, 1, argb);
			rect(x + 6 - i, y + i, 1, 1, argb);
		}
	}
	/** 字体渲染会把 α 低于 4 的颜色补成不透明，淡入淡出时会在阈值附近闪一下，低于该值直接不画。 */
	private static final int MIN_TEXT_ALPHA = 8;
	public void text(String text, int x, int y, int color) {
		text(text, x, y, color, false);
	}
	public void text(String text, int x, int y, int color, boolean shadow) {
		if (text == null || text.isEmpty()) return;
		var argb = col(color);
		if (argb >>> 24 < MIN_TEXT_ALPHA) return;
		gui.drawString(ClientUtil.mc.font, text, x, y, argb, shadow);
	}
	public void text(Component text, int x, int y, int color) {
		text(text, x, y, color, false);
	}
	public void text(Component text, int x, int y, int color, boolean shadow) {
		var argb = col(color);
		if (argb >>> 24 < MIN_TEXT_ALPHA) return;
		gui.drawString(ClientUtil.mc.font, text, x, y, argb, shadow);
	}
	public void text(FormattedCharSequence text, int x, int y, int color) {
		var argb = col(color);
		if (argb >>> 24 < MIN_TEXT_ALPHA) return;
		gui.drawString(ClientUtil.mc.font, text, x, y, argb, false);
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
	public void icon(FlexGuiTextures icon, UiBounds bounds, int argb) {
		if (icon == null || alpha <= 0.004F) return;
		var color = col(argb);
		RenderSystem.setShaderColor(
			(color >> 16 & 0xFF) / 255F,
			(color >> 8 & 0xFF) / 255F,
			(color & 0xFF) / 255F,
			(color >>> 24 & 0xFF) / 255F
		);
		texture(
			icon.texture, new UiBounds(
				bounds.x() + (bounds.width() - icon.width()) / 2,
				bounds.y() + (bounds.height() - icon.height()) / 2,
				icon.width(),
				icon.height()
			)
		);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
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
