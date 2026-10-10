package io.github.forgestove.flexconfig.client.gui;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.forgestove.flexconfig.client.ClientUtil;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public final class ToggleSwitch extends AbstractWidget {
	/** 滑动时长与缓动曲线，对应 CSS 的 125ms cubic-bezier(.34, 1.56, .64, 1)。 */
	private static final float SLIDE_MS = 125f;
	private static final float CURVE_X1 = 0.34f, CURVE_Y1 = 1.56f, CURVE_X2 = 0.64f;
	private final Consumer<Boolean> onToggle;
	private boolean value;
	private boolean sliding;
	private long slideStart;
	private float slideFrom;
	/** 钮的位置：0 = 关，1 = 开，中间为动画值（弹性段会略微越界）。 */
	private float progress;
	public ToggleSwitch(int width, int height, Component message, boolean initialValue, Consumer<Boolean> onToggle) {
		super(0, 0, width, height, message);
		value = initialValue;
		progress = initialValue ? 1f : 0f;
		this.onToggle = onToggle;
	}
	/** 外部同步值（不触发回调），供 refresh 调用。 */
	public void setValue(boolean newValue) {
		if (value == newValue) return;
		value = newValue;
		startSlide();
	}
	/**
	 * NeoForge 弃用了这个两参重载并建议改用 {@code onClick(double, double, int)}，但三参版本只由
	 * NeoForge 的 {@code IAbstractWidgetExtension} 注入，Fabric 侧没有，共享源码只能重写这个。
	 * 三参版的默认实现就是转调此处，行为一致。
	 */
	@Override
	@SuppressWarnings("deprecation")
	public void onClick(double mouseX, double mouseY) {
		value = !value;
		startSlide();
		onToggle.accept(value);
	}
	private void startSlide() {
		slideFrom = progress;
		slideStart = Util.getMillis();
		sliding = true;
	}
	@Override
	protected void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		stepSlide();
		// 尺寸也归纹理表管（= atlas region），每帧读，资源包改 region 即生效
		var trackW = AllGuiTextures.TOGGLE_TRACK_OFF.width();
		var trackH = AllGuiTextures.TOGGLE_TRACK_OFF.height();
		var thumbSize = AllGuiTextures.TOGGLE_THUMB.width();
		var trackX = getX() + (width - trackW) / 2;
		// 轨道底边与滑钮底边齐平（官方是居中、上下各溢出 1px），钮只在顶部高出
		var trackY = getY() + thumbSize - trackH;
		renderTrack(gui, trackX, trackY);
		// 行程 = 轨道宽 − 钮宽：关态钮左缘、开态钮右缘都与轨道边缘齐平
		var thumbX = trackX + Math.round((trackW - thumbSize) * progress);
		renderThumb(gui, thumbX, getY());
		if (isFocused()) {
			// 聚焦环精灵比钮大 (间隙 + 环宽) × 2，居中对齐到钮
			var ring = AllGuiTextures.TOGGLE_FOCUS_RING;
			var inset = (ring.width() - thumbSize) / 2;
			ring.render(gui, thumbX - inset, getY() - inset);
		}
	}
	/** 按真实时间推进滑钮位置；弹性曲线会在中段略微冲过目标再回弹。 */
	private void stepSlide() {
		var target = value ? 1f : 0f;
		if (!sliding) {
			progress = target;
			return;
		}
		var t = (Util.getMillis() - slideStart) / SLIDE_MS;
		if (t >= 1f) {
			progress = target;
			sliding = false;
			return;
		}
		progress = slideFrom + (target - slideFrom) * ease(t);
	}
	/** 先铺关闭态，再按进度以 alpha 叠加开启态 —— 视觉上等价于官方「滑钮遮住不该显色的一半」，但带过渡。 */
	private void renderTrack(@NotNull GuiGraphics gui, int x, int y) {
		if (!active) {
			AllGuiTextures.TOGGLE_TRACK_DISABLED.render(gui, x, y);
			return;
		}
		AllGuiTextures.TOGGLE_TRACK_OFF.render(gui, x, y);
		var alpha = Mth.clamp(progress, 0f, 1f);
		if (alpha <= 0f) return;
		RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
		AllGuiTextures.TOGGLE_TRACK_ON.render(gui, x, y);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
	}
	/** 滑钮：禁用 > 按下 > 悬停 > 常态；聚焦只在外面加白环、不改底色。 */
	private void renderThumb(@NotNull GuiGraphics gui, int x, int y) {
		var texture = !active
			? AllGuiTextures.TOGGLE_THUMB_DISABLED
			: isPressed()
				? AllGuiTextures.TOGGLE_THUMB_PRESSED
				: isHovered() ? AllGuiTextures.TOGGLE_THUMB_HOVER : AllGuiTextures.TOGGLE_THUMB;
		texture.render(gui, x, y);
	}
	/** 物理左键按住且指针在钮上即为按下态 —— 比自己维护 state 稳，不会漏掉在控件外松开的那次。 */
	private boolean isPressed() {
		return isHovered() && ClientUtil.mc.mouseHandler.isLeftPressed();
	}
	@Override
	protected void updateWidgetNarration(@NotNull NarrationElementOutput narration) {
		defaultButtonNarrationText(narration);
	}
	/** 求解 cubic-bezier(.34, 1.56, .64, 1)：先用二分法由 x 反解参数 t，再取其 y。 */
	private static float ease(float x) {
		var lo = 0f;
		var hi = 1f;
		for (var i = 0; i < 16; i++) {
			var mid = (lo + hi) / 2;
			if (bezier(CURVE_X1, CURVE_X2, mid) < x) lo = mid;
			else hi = mid;
		}
		return bezier(CURVE_Y1, 1f, (lo + hi) / 2);
	}
	private static float bezier(float p1, float p2, float t) {
		var u = 1 - t;
		return 3 * u * u * t * p1 + 3 * u * t * t * p2 + t * t * t;
	}
}
