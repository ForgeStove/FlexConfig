package io.github.forgestove.flexconfig.foundation.ui.core;
import org.jetbrains.annotations.Nullable;

import java.util.*;
/** 所有界面都通过这里绘制。透明度和缩放在这里统一处理，具体图形操作交给 UiRenderTarget。 */
public class UiPainter {
	private final UiRenderTarget target;
	private final ArrayDeque<Float> alphaStack = new ArrayDeque<>();
	private float alpha = 1F;
	private float scale = 1F;
	private int scaleDepth;
	public UiPainter(UiRenderTarget target) {
		this.target = target;
	}
	public void pushAlpha(float factor) {
		alphaStack.push(alpha);
		alpha *= Math.clamp(factor, 0F, 1F);
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
		target.pushPose();
		if (factor != 1F) {
			target.scale(factor, factor, 1F);
			scale *= factor;
		}
		scaleDepth++;
	}
	public void popScale() {
		if (scaleDepth == 0) return;
		flush();
		target.popPose();
		scaleDepth--;
		if (scaleDepth == 0) scale = 1F;
	}
	public void pushTranslate(float x, float y) {
		flush();
		target.pushPose();
		target.translate(x, y, 0);
	}
	public void popTranslate() {
		flush();
		target.popPose();
	}
	public void pushZ(float z) {
		flush();
		target.pushPose();
		target.translate(0, 0, z);
	}
	public void popZ() {
		flush();
		target.popPose();
	}
	public void rect(UiBounds bounds, int color) {
		rect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), color);
	}
	public void rect(int x, int y, int width, int height, int color) {
		if (width <= 0 || height <= 0 || alpha <= 0.004F) return;
		target.fill(x, y, x + width, y + height, col(color));
	}
	public int @Nullable [] textureSize(@Nullable UiTexture texture) {
		if (texture == null) return null;
		var width = texture.width();
		var height = texture.height();
		return width <= 0 || height <= 0 ? null : new int[]{width, height};
	}
	public void texture(UiTexture texture, UiBounds bounds) {
		if (texture == null || bounds.width() <= 0 || bounds.height() <= 0 || alpha <= 0.004F) return;
		var dims = textureSize(texture);
		if (dims == null) return;
		target.blit(texture, bounds.x(), bounds.y(), bounds.width(), bounds.height(), 0F, 0F, dims[0], dims[1], dims[0], dims[1]);
	}
	public void gradient(UiBounds bounds, int top, int bottom) {
		if (bounds.width() <= 0 || bounds.height() <= 0 || alpha <= 0.004F) return;
		target.fillGradient(bounds.x(), bounds.y(), bounds.right(), bounds.bottom(), col(top), col(bottom));
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
		text(UiText.literal(text), x, y, color);
	}
	public void text(String text, int x, int y, int color, boolean shadow) {
		text(UiText.literal(text), x, y, color, shadow);
	}
	public void text(UiText text, int x, int y, int color) {
		text(text, x, y, color, false);
	}
	public void text(UiText text, int x, int y, int color, boolean shadow) {
		if (text == null || text.isEmpty()) return;
		var argb = col(color);
		if (argb >>> 24 < MIN_TEXT_ALPHA) return;
		target.text(text, x, y, argb, shadow);
	}
	public void textEmphasis(String text, int x, int y, int color) {
		text(UiText.literal(text).withStyle(UiStyle.BOLD), x, y, color);
	}
	public void textEmphasis(UiText text, int x, int y, int color) {
		text(text.copy().withStyle(UiStyle.BOLD), x, y, color);
	}
	public int textWidthEmphasis(String text) {
		return textWidth(UiText.literal(text).withStyle(UiStyle.BOLD));
	}
	public void textRight(UiText text, int right, int y, int color) {
		text(text, right - textWidth(text), y, color);
	}
	public int textWidth(String text) {
		return UiTexts.width(text);
	}
	public int textWidth(UiText text) {
		return UiTexts.width(text);
	}
	public int lineHeight() {
		return UiTexts.lineHeight();
	}
	public String trim(String text, int maxWidth) {
		return UiTexts.trim(text, maxWidth);
	}
	public List<UiText> split(UiText text, int maxWidth) {
		return UiTexts.split(text, maxWidth);
	}
	public void icon(@Nullable UiTexture icon, UiBounds bounds, int argb) {
		if (icon == null || alpha <= 0.004F) return;
		var color = col(argb);
		target.setShaderColor((color >> 16 & 0xFF) / 255F, (color >> 8 & 0xFF) / 255F, (color & 0xFF) / 255F,
			(color >>> 24 & 0xFF) / 255F);
		texture(
			icon, new UiBounds(
				bounds.x() + (bounds.width() - icon.width()) / 2,
				bounds.y() + (bounds.height() - icon.height()) / 2,
				icon.width(),
				icon.height()
			)
		);
		target.setShaderColor(1F, 1F, 1F, 1F);
	}
	public void scissorOn(UiBounds bounds) {
		flush();
		target.enableScissor(
			Math.round(bounds.x() * scale),
			Math.round(bounds.y() * scale),
			Math.round(bounds.right() * scale),
			Math.round(bounds.bottom() * scale)
		);
	}
	public void scissorOff() {
		flush();
		target.disableScissor();
	}
	public void flush() {
		target.flush();
	}
}
