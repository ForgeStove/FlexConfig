package io.github.forgestove.flexconfig.client.gui.components;
import io.github.forgestove.flexconfig.client.gui.FlexGuiTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;
/**
 * 每状态一张完整贴图加九宫格元数据（横向拉伸）：宽度归控件，配色与分层均由纹理提供。按下态无底部暗带而
 * 矮 2px 且底对齐，禁用态无高光；深色类型另有一张深色禁用图。
 */
public class OreButton extends OreAbstractButton {
	public static final int DEFAULT_WIDTH = 150;
	/** [类型][状态]：行序同 {@link Style}，列序为常态 / 悬停 / 按下。 */
	private static final FlexGuiTextures[][] BOXES = {
		{FlexGuiTextures.BUTTON_PRIMARY, FlexGuiTextures.BUTTON_PRIMARY_HOVER, FlexGuiTextures.BUTTON_PRIMARY_PRESSED},
		{FlexGuiTextures.BUTTON_SECONDARY, FlexGuiTextures.BUTTON_SECONDARY_HOVER, FlexGuiTextures.BUTTON_SECONDARY_PRESSED},
		{FlexGuiTextures.BUTTON_DESTRUCTIVE, FlexGuiTextures.BUTTON_DESTRUCTIVE_HOVER, FlexGuiTextures.BUTTON_DESTRUCTIVE_PRESSED},
		{FlexGuiTextures.BUTTON_BLACK, FlexGuiTextures.BUTTON_BLACK_HOVER, FlexGuiTextures.BUTTON_BLACK_PRESSED},
	};
	/** 行序同 {@link Style}。 */
	private static final FlexGuiTextures[] DISABLED_BOXES = {
		FlexGuiTextures.BUTTON_DISABLED,
		FlexGuiTextures.BUTTON_DISABLED,
		FlexGuiTextures.BUTTON_DISABLED,
		FlexGuiTextures.BUTTON_BLACK_DISABLED,
	};
	private static final int RING_INSET = 1, RING_COLOR = 0xFFFFFFFF;
	private static final int TEXT_LIGHT = 0xFFFFFFFF, TEXT_DARK = 0xFF1E1E1F;
	private static final int TEXT_DISABLED = 0xFF48494A, TEXT_BLACK_DISABLED = 0xFF8C8D90;
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
		return FlexGuiTextures.BUTTON_PRIMARY.height();
	}
	/** 对齐原版 {@code Button.builder} 的链路（size / bounds / tooltip / build），便于整批替换。 */
	public static Builder of(@NotNull Component message, @NotNull OnPress onPress) {
		return new Builder(message, onPress);
	}
	@Override
	public void onPress() {
		onPress.onPress(this);
	}
	@Override
	protected void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		var box = box();
		var height = box.height();
		var y = getY() + getHeight() - height;   // 盒子底对齐：按下态矮 2px 就沉下去，控件加高也能跟着长
		box.render(gui, getX(), y, getWidth(), height);
		renderContent(gui, y);
		if (shouldRenderFocus()) renderRing(gui, y, height);
		renderCursor();
	}
	public void setStyle(@NotNull Style style) {
		this.style = style;
	}
	/** 按下要排在聚焦之前，否则点一下拿到焦点后，按下态会被聚焦态盖掉。 */
	private FlexGuiTextures box() {
		if (!active) return DISABLED_BOXES[style.ordinal()];
		var states = BOXES[style.ordinal()];
		if (isPressed()) return states[2];
		if (shouldRenderFocus()) return states[0];
		return states[isHovered() ? 1 : 0];
	}
	/**
	 * {@code boxY} 指盒子顶端；按下态盒子矮 2px 又底对齐，故该值随状态变，内容纵坐标须经 {@link #centeredY} 折算。
	 */
	protected void renderContent(@NotNull GuiGraphics gui, int boxY) {
		renderButtonString(gui, boxY);
	}
	private void renderRing(@NotNull GuiGraphics gui, int y, int height) {
		gui.renderOutline(getX() - RING_INSET, y - RING_INSET, getWidth() + 2 * RING_INSET, height + 2 * RING_INSET, RING_COLOR);
	}
	private void renderButtonString(@NotNull GuiGraphics gui, int y) {
		renderScrollingString(
			gui,
			Minecraft.getInstance().font,
			getMessage(),
			getX() + TEXT_MARGIN,
			y + TEXT_TOP,
			getX() + getWidth() - TEXT_MARGIN,
			y + TEXT_BOTTOM,
			textColor()
		);
	}
	protected int textColor() {
		if (!active) return style == Style.BLACK ? TEXT_BLACK_DISABLED : TEXT_DISABLED;
		return style == Style.SECONDARY ? TEXT_DARK : TEXT_LIGHT;
	}
	/**
	 * 内容区（盒顶 +{@link #TEXT_TOP} 至盒顶 +{@link #TEXT_BOTTOM}）内垂直居中一个高 {@code height} 的内容。
	 * 文字与图标共用：内容区相对盒顶恒定，按下态盒子变矮时内容只随盒顶平移。
	 */
	protected final int centeredY(int boxY, int height) {
		return boxY + (TEXT_TOP + TEXT_BOTTOM - height) / 2;
	}
	public enum Style {
		PRIMARY,
		SECONDARY,
		DESTRUCTIVE,
		BLACK
	}
	@FunctionalInterface
	public interface OnPress {
		void onPress(OreButton button);
	}
	@SuppressWarnings("unused")
	public static final class Builder {
		private final Component message;
		private final OnPress onPress;
		@Nullable private Tooltip tooltip;
		private int x, y, width = DEFAULT_WIDTH, height = preferredHeight();
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
