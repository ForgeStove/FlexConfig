package io.github.forgestove.flexconfig.client.gui.im.core;
import io.github.forgestove.flexconfig.api.Accent;
import io.github.forgestove.flexconfig.client.ClientUtil;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
/** 跨帧保留的界面上下文：焦点、拖拽 owner、提示延迟和淡入都由界面复用同一个实例。 */
public class UiContext {
	public GuiGraphics graphics;
	public UiPainter painter;
	public float delta;
	public double mouseX;
	public double mouseY;
	public final UiFocus focus = new UiFocus();
	private final UiTween tooltipFade = new UiTween(0);
	private @Nullable Component candidate;
	private @Nullable Component shown;
	private long candidateSince;
	private boolean requestedThisFrame;
	private boolean focusTooltip;
	private UiBounds tooltipAnchor = UiBounds.EMPTY;
	private int tooltipX;
	private int tooltipY;
	private long lastClosedAt = -1_000_000;
	private int viewportWidth, viewportHeight;
	private Object dragOwner;
	private boolean focusVisible;
	private Accent accent = Accent.GOLD;
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
		requestedThisFrame = false;
	}
	public void viewport(int width, int height) {
		viewportWidth = width;
		viewportHeight = height;
	}
	public void pointer(double mouseX, double mouseY) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}
	public void tooltip(Component text) {
		tooltip(text, UiBounds.EMPTY);
	}
	public void tooltip(Component text, UiBounds anchor) {
		requestTooltip(text, false, anchor);
	}
	public void tooltipFocused(Component text, UiBounds anchor) {
		requestTooltip(text, true, anchor);
	}
	private void requestTooltip(Component text, boolean focus, UiBounds anchor) {
		if (!text.equals(candidate) || focus != focusTooltip) {
			if (shown != null) lastClosedAt = Util.getMillis();
			candidate = text;
			candidateSince = Util.getMillis();
			shown = null;
			tooltipFade.snap(0);
		}
		requestedThisFrame = true;
		focusTooltip = focus;
		tooltipAnchor = anchor;
		tooltipX = (int) mouseX;
		tooltipY = (int) mouseY;
	}
	/** 鼠标移开或点击时调用，清掉候选提示，已显示的提示淡出。 */
	public void clearTooltip() {
		if (shown != null) lastClosedAt = Util.getMillis();
		candidate = null;
		requestedThisFrame = false;
	}
	public @Nullable Component tooltip() {
		return requestedThisFrame ? candidate : shown;
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
		if (requestedThisFrame && candidate != null && (shown != candidate || tooltipFade.target() < 0.5F)) {
			var delay = now - lastClosedAt < 300 ? 0F : focusTooltip ? UiMotion.TOOLTIP_FOCUS_DELAY : UiMotion.TOOLTIP_HOVER_DELAY;
			if (now - candidateSince >= (long) delay) {
				shown = candidate;
				tooltipFade.start(tooltipFade.value(), 1, UiMotion.TOOLTIP_FADE);
			}
		} else if (!requestedThisFrame) {
			tooltipFade.start(tooltipFade.value(), 0, UiMotion.TOOLTIP_FADE);
			if (tooltipFade.done()) shown = null;
		}
		tooltipFade.update(delta);
		if (shown == null || tooltipFade.value() <= 0.01F || painter == null) return;
		painter.pushAlpha(tooltipFade.value());
		UiTooltipRenderer.render(
			painter,
			shown,
			tooltipAnchor,
			tooltipX,
			tooltipY,
			viewportWidth > 0 ? viewportWidth : ClientUtil.mc.getWindow().getGuiScaledWidth(),
			viewportHeight > 0 ? viewportHeight : ClientUtil.mc.getWindow().getGuiScaledHeight(),
			!focusTooltip
		);
		painter.popAlpha();
	}
}
