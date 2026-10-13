package io.github.forgestove.flexconfig.client.gui.components;
import io.github.forgestove.flexconfig.client.gui.FlexGuiTextures;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.*;
/**
 * 普通 Ore 按钮。
 * <p>
 * 每状态一张完整贴图 + atlas 三段切（左帽 2 = 描边+亮边、中段 1 = 填充、右帽 2 = 弱高光+描边），
 * 横向宽度归控件、配色全在纹理里。按下态没有底部暗带（矮 2px、底对齐）；禁用态没有高光、三类型共用一张图。
 */
public class OreButton extends OreAbstractButton {
	private static final Box PRIMARY_NORMAL = new Box(
		FlexGuiTextures.BUTTON_PRIMARY_L,
		FlexGuiTextures.BUTTON_PRIMARY_M,
		FlexGuiTextures.BUTTON_PRIMARY_R
	);
	private static final Box PRIMARY_HOVER = new Box(
		FlexGuiTextures.BUTTON_PRIMARY_HOVER_L,
		FlexGuiTextures.BUTTON_PRIMARY_HOVER_M,
		FlexGuiTextures.BUTTON_PRIMARY_HOVER_R
	);
	private static final Box PRIMARY_PRESSED = new Box(
		FlexGuiTextures.BUTTON_PRIMARY_PRESSED_L,
		FlexGuiTextures.BUTTON_PRIMARY_PRESSED_M,
		FlexGuiTextures.BUTTON_PRIMARY_PRESSED_R
	);
	private static final Box SECONDARY_NORMAL = new Box(
		FlexGuiTextures.BUTTON_SECONDARY_L,
		FlexGuiTextures.BUTTON_SECONDARY_M,
		FlexGuiTextures.BUTTON_SECONDARY_R
	);
	private static final Box SECONDARY_HOVER = new Box(
		FlexGuiTextures.BUTTON_SECONDARY_HOVER_L,
		FlexGuiTextures.BUTTON_SECONDARY_HOVER_M,
		FlexGuiTextures.BUTTON_SECONDARY_HOVER_R
	);
	private static final Box SECONDARY_PRESSED = new Box(
		FlexGuiTextures.BUTTON_SECONDARY_PRESSED_L,
		FlexGuiTextures.BUTTON_SECONDARY_PRESSED_M,
		FlexGuiTextures.BUTTON_SECONDARY_PRESSED_R
	);
	private static final Box DESTRUCTIVE_NORMAL = new Box(
		FlexGuiTextures.BUTTON_DESTRUCTIVE_L,
		FlexGuiTextures.BUTTON_DESTRUCTIVE_M,
		FlexGuiTextures.BUTTON_DESTRUCTIVE_R
	);
	private static final Box DESTRUCTIVE_HOVER = new Box(
		FlexGuiTextures.BUTTON_DESTRUCTIVE_HOVER_L,
		FlexGuiTextures.BUTTON_DESTRUCTIVE_HOVER_M,
		FlexGuiTextures.BUTTON_DESTRUCTIVE_HOVER_R
	);
	private static final Box DESTRUCTIVE_PRESSED = new Box(
		FlexGuiTextures.BUTTON_DESTRUCTIVE_PRESSED_L,
		FlexGuiTextures.BUTTON_DESTRUCTIVE_PRESSED_M,
		FlexGuiTextures.BUTTON_DESTRUCTIVE_PRESSED_R
	);
	private static final Box DISABLED = new Box(
		FlexGuiTextures.BUTTON_DISABLED_L,
		FlexGuiTextures.BUTTON_DISABLED_M,
		FlexGuiTextures.BUTTON_DISABLED_R
	);
	/** [类型][状态]，行序同 {@link Style}，列序为常态 / 悬停 / 按下。 */
	private static final Box[][] BOXES = {
		{PRIMARY_NORMAL, PRIMARY_HOVER, PRIMARY_PRESSED},
		{SECONDARY_NORMAL, SECONDARY_HOVER, SECONDARY_PRESSED},
		{DESTRUCTIVE_NORMAL, DESTRUCTIVE_HOVER, DESTRUCTIVE_PRESSED},
	};
	private static final int RING_INSET = 1, RING_COLOR = 0xFFFFFFFF;
	private static final int TEXT_LIGHT = 0xFFFFFFFF, TEXT_DARK = 0xFF1E1E1F, TEXT_DISABLED = 0xFF48494A;
	private static final int TEXT_MARGIN = 2;
	/** 文字内容区相对盒顶的上下界。两态盒高不同（20 / 18），按固定内容区居中才不会因取整差 1px。 */
	private static final int TEXT_TOP = 2, TEXT_BOTTOM = 16;
	private final OnPress onPress;
	private Style style;
	public OreButton(int width, @NotNull Component message, @NotNull Style style, @NotNull OnPress onPress) {
		super(0, 0, width, preferredHeight(), message);
		this.style = style;
		this.onPress = onPress;
	}
	public static int preferredHeight() {
		return FlexGuiTextures.BUTTON_PRIMARY_L.height();
	}
	/** 对齐原版 {@code Button.builder} 的链路（size / bounds / tooltip / build），便于整批替换。 */
	public static Builder of(@NotNull Component message, @NotNull OnPress onPress) {
		return new Builder(message, onPress);
	}
	@Override
	public void onPress() {
		onPress.onPress(this);
	}
	public void setStyle(@NotNull Style style) {
		this.style = style;
	}
	@Override
	protected void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		var box = box();
		var capW = box.left().width();
		var height = box.left().height();
		var y = getY() + preferredHeight() - height;   // 按下态的盒子矮 2px 并底对齐，所以看起来是沉下去
		box.left().render(gui, getX(), y);
		box.mid().render(gui, getX() + capW, y, getWidth() - 2 * capW, height);
		box.right().render(gui, getX() + getWidth() - capW, y);
		renderContent(gui, y);
		if (shouldRenderFocus()) renderRing(gui, y, height);
		renderCursor();
	}
	/** 按下要排在聚焦之前，否则点一下拿到焦点后，按下态会被聚焦态盖掉。 */
	private Box box() {
		if (!active) return DISABLED;
		var states = BOXES[style.ordinal()];
		if (isPressed()) return states[2];
		if (shouldRenderFocus()) return states[0];
		return states[isHovered() ? 1 : 0];
	}
	/**
	 * 画盒子里的内容（默认居中文字）；{@code boxY} 是**盒子**顶端 —— 按下态盒子矮 2px 且底对齐，所以它随状态变，
	 * 内容位置一律由 {@link #centeredY} 算。
	 */
	protected void renderContent(@NotNull GuiGraphics gui, int boxY) {
		renderButtonString(gui, boxY);
	}
	private void renderRing(@NotNull GuiGraphics gui, int y, int height) {
		gui.renderOutline(getX() - RING_INSET, y - RING_INSET, getWidth() + 2 * RING_INSET, height + 2 * RING_INSET, RING_COLOR);
	}
	/** 原版 {@code renderScrollingString} 画字固定带阴影，这里复刻"居中 + 超长滚动 + 裁剪"并去掉阴影。 */
	private void renderButtonString(@NotNull GuiGraphics gui, int y) {
		var font = Minecraft.getInstance().font;
		var text = getMessage();
		var color = textColor();
		var minX = getX() + TEXT_MARGIN;
		var maxX = getX() + getWidth() - TEXT_MARGIN;
		var top = y + TEXT_TOP;
		var bottom = y + TEXT_BOTTOM;
		var textY = centeredY(y, 9) + 1;   // +1 = 原版 renderScrollingString 的排版补偿（字高 9、可见 8）
		var room = maxX - minX;
		var textW = font.width(text);
		gui.enableScissor(minX, top, maxX, bottom);
		if (textW > room) {
			var over = textW - room;
			var seconds = Util.getMillis() / 1000.0;
			var period = Math.max(over * 0.5, 3.0);
			var phase = Math.sin(Math.PI / 2 * Math.cos(Math.PI * 2 * seconds / period)) / 2.0 + 0.5;
			gui.drawString(font, text, minX - (int) Mth.lerp(phase, 0.0, over), textY, color, false);
		} else gui.drawString(font, text, (minX + maxX) / 2 - textW / 2, textY, color, false);
		gui.disableScissor();
	}
	protected int textColor() {
		if (!active) return TEXT_DISABLED;
		return style == Style.SECONDARY ? TEXT_DARK : TEXT_LIGHT;
	}
	/**
	 * 内容区（盒顶 +{@link #TEXT_TOP} .. 盒顶 +{@link #TEXT_BOTTOM}）里垂直居中一个高 {@code height} 的内容。
	 * <p>
	 * 文字与图标共用这一套：内容区**相对盒顶恒定**，所以按下态盒子变矮时内容只随盒顶平移，不会自己往上收。
	 */
	protected final int centeredY(int boxY, int height) {
		return boxY + (TEXT_TOP + TEXT_BOTTOM - height) / 2;
	}
	public enum Style {
		PRIMARY,
		SECONDARY,
		DESTRUCTIVE
	}
	@FunctionalInterface
	public interface OnPress {
		void onPress(OreButton button);
	}
	/** 一个状态的三段：左帽、可拉伸中段、右帽。 */
	private record Box(FlexGuiTextures left, FlexGuiTextures mid, FlexGuiTextures right) {}
	public static final class Builder {
		private final Component message;
		private final OnPress onPress;
		@Nullable private Tooltip tooltip;
		private int x, y, width = 150, height = preferredHeight();
		private Builder(Component message, OnPress onPress) {
			this.message = message;
			this.onPress = onPress;
		}
		public Builder width(int width) {
			this.width = width;
			return this;
		}
		public Builder size(int width, int height) {
			this.width = width;
			this.height = height;
			return this;
		}
		public Builder bounds(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			return this;
		}
		public Builder tooltip(@Nullable Tooltip tooltip) {
			this.tooltip = tooltip;
			return this;
		}
		public OreButton build() {
			var button = new OreButton(width, message, Style.SECONDARY, onPress);
			button.setPosition(x, y);
			button.setHeight(height);
			button.setTooltip(tooltip);
			return button;
		}
	}
}
