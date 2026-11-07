package io.github.forgestove.flexconfig.foundation.ui.core;
import io.github.forgestove.flexconfig.api.menu.Accent;
import io.github.forgestove.flexconfig.foundation.client.ClientUtil;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
/** 跨帧保留的界面上下文：焦点、拖拽 owner、提示延迟和淡入都由界面复用同一个实例。 */
public class UiContext {
	public GuiGraphics graphics;
	public UiPainter painter;
	public float delta;
	public double mouseX;
	public double mouseY;
	public final UiFocus focus = new UiFocus();
	private final UiTween tooltipFade = new UiTween(0);
	private @Nullable Object hoveredOwner, focusedOwner, shownFor;
	private @Nullable Supplier<Component> hoveredContent, focusedContent, shownContent;
	private UiBounds hoveredAnchor = UiBounds.EMPTY, focusedAnchor = UiBounds.EMPTY, shownAnchor = UiBounds.EMPTY;
	private @Nullable Object candidateOwner;
	private long candidateSince;
	private boolean candidateByFocus;
	private long lastClosedAt = -1_000_000;
	private int viewportWidth, viewportHeight;
	private Object dragOwner;
	private boolean focusVisible;
	private Accent accent = Accent.DEFAULT;
	private Runnable refresh = () -> {};
	public Accent accent() {
		return accent;
	}
	public void setAccent(Accent accent) {
		this.accent = accent;
	}
	public void beginFrame(GuiGraphics graphics, float delta, double mouseX, double mouseY) {
		this.graphics = graphics;
		painter = new UiPainter(graphics);
		this.delta = delta;
		pointer(mouseX, mouseY);
		hoveredOwner = null;
		focusedOwner = null;
	}
	public void viewport(int width, int height) {
		viewportWidth = width;
		viewportHeight = height;
	}
	public void pointer(double mouseX, double mouseY) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}
	public void hover(Object owner, Supplier<Component> content, UiBounds anchor) {
		if (hoveredOwner != null && hoveredOwner != owner) return;
		hoveredOwner = owner;
		hoveredContent = content;
		hoveredAnchor = anchor;
	}
	public void focusTarget(Object owner, Supplier<Component> content, UiBounds anchor) {
		focusedOwner = owner;
		focusedContent = content;
		focusedAnchor = anchor;
	}
	/** 鼠标移开或点击时调用，清掉候选提示，已显示的提示淡出。 */
	public void clearTooltip() {
		if (shownFor != null) lastClosedAt = Util.getMillis();
		hoveredOwner = null;
		focusedOwner = null;
		candidateOwner = null;
		shownFor = null;
		shownContent = null;
	}
	public boolean focusVisible() {
		return focusVisible;
	}
	public void setFocusVisible(boolean visible) {
		focusVisible = visible;
	}
	public void setDragOwner(@Nullable Object owner) {
		dragOwner = owner;
	}
	public @Nullable Object dragOwner() {
		return dragOwner;
	}
	public void refresh() {
		refresh.run();
	}
	public void onRefresh(Runnable refresh) {
		this.refresh = refresh;
	}
	public void renderTooltip() {
		var now = Util.getMillis();
		var byFocus = hoveredOwner == null;
		var owner = byFocus ? focusedOwner : hoveredOwner;
		var content = byFocus ? focusedContent : hoveredContent;
		var anchor = byFocus ? focusedAnchor : hoveredAnchor;
		if (owner == null || content == null) {
			candidateOwner = null;
			if (shownFor != null) {
				shownFor = null;
				shownContent = null;
				lastClosedAt = now;
				tooltipFade.start(tooltipFade.value(), 0, UiMotion.TOOLTIP_FADE);
			}
		} else {
			if (owner != candidateOwner) {
				if (shownFor != null) lastClosedAt = now;
				candidateOwner = owner;
				candidateSince = now;
				candidateByFocus = byFocus;
				shownFor = null;
				shownContent = null;
				tooltipFade.snap(0);
			}
			if (shownFor == owner) {
				shownContent = content;
				shownAnchor = anchor;
			} else {
				var delay = now - lastClosedAt < 300 ? 0F : byFocus ? UiMotion.TOOLTIP_FOCUS_DELAY : UiMotion.TOOLTIP_HOVER_DELAY;
				var text = now - candidateSince >= (long) delay ? content.get() : null;
				if (text != null) {
					shownFor = owner;
					shownContent = content;
					shownAnchor = anchor;
					tooltipFade.start(tooltipFade.value(), 1, UiMotion.TOOLTIP_FADE);
				}
			}
		}
		tooltipFade.update(delta);
		var text = shownContent == null ? null : shownContent.get();
		if (text == null) {
			if (shownFor != null && tooltipFade.target() > 0) tooltipFade.start(tooltipFade.value(), 0, UiMotion.TOOLTIP_FADE);
			shownFor = null;
			return;
		}
		if (tooltipFade.value() <= 0.01F || painter == null) return;
		painter.pushAlpha(tooltipFade.value());
		UiTooltipRenderer.render(
			painter,
			text,
			shownAnchor,
			(int) mouseX,
			(int) mouseY,
			viewportWidth > 0 ? viewportWidth : ClientUtil.mc.getWindow().getGuiScaledWidth(),
			viewportHeight > 0 ? viewportHeight : ClientUtil.mc.getWindow().getGuiScaledHeight(),
			!candidateByFocus
		);
		painter.popAlpha();
	}
}
