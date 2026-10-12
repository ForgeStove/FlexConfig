package io.github.forgestove.flexconfig.client.gui.components;
import io.github.forgestove.flexconfig.client.gui.components.OreButton.Style;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
/** 原版 {@link ConfirmScreen} 的 Ore UI 版：两个按钮换成 {@link OreButton}，布局同父类（宽 150、间距 10、居中）。 */
public final class OreConfirmScreen extends ConfirmScreen {
	private static final int BUTTON_WIDTH = 150, BUTTON_GAP = 10;
	/** 肯定按钮的配色：不可逆的用 Destructive，普通确认用 Primary。 */
	private final Style yesStyle;
	public OreConfirmScreen(
		@NotNull BooleanConsumer callback,
		@NotNull Component title,
		@NotNull Component message,
		@NotNull Component yesLabel,
		@NotNull Style yesStyle,
		@NotNull Component noLabel
	) {
		super(callback, title, message, yesLabel, noLabel);
		this.yesStyle = yesStyle;
	}
	/** 覆写整个 addButtons：父类的 {@code addExitButton} 只收原版 {@code Button}，塞不进 {@link OreButton}。 */
	@Override
	protected void addButtons(int y) {
		var x = (width - (BUTTON_WIDTH * 2 + BUTTON_GAP)) / 2;
		var yes = new OreButton(BUTTON_WIDTH, yesButton, yesStyle, b -> callback.accept(true));
		yes.setPosition(x, y);
		addRenderableWidget(yes);
		var no = new OreButton(BUTTON_WIDTH, noButton, Style.SECONDARY, b -> callback.accept(false));
		no.setPosition(x + BUTTON_WIDTH + BUTTON_GAP, y);
		addRenderableWidget(no);
	}
}
