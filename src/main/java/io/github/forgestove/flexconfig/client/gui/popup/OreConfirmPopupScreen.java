package io.github.forgestove.flexconfig.client.gui.popup;
import io.github.forgestove.flexconfig.client.gui.components.OreButton;
import io.github.forgestove.flexconfig.client.gui.components.OreButton.Style;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.List;
/**
 * 确认弹窗：标题栏 + 自动换行的正文 + 两个 {@link OreButton}，居中模态。
 * <p>
 * 按钮只转调 {@code callback}，**不自动关窗** —— 回调自己负责切换界面（与调用方约定一致）。
 */
public class OreConfirmPopupScreen extends OrePopupScreen {
	private static final int WIDTH = 300, BUTTON_MARGIN = 4, BUTTON_GAP = 4, MESSAGE_GAP = 12, LINE_GAP = 1;
	private static final int TEXT_COLOR = 0xFFE0E0E0;
	private final BooleanConsumer callback;
	private final Component message, yesLabel, noLabel;
	private final Style yesStyle;
	private List<FormattedCharSequence> lines = List.of();
	private OreButton yesButton, noButton;
	public OreConfirmPopupScreen(
		@NotNull Screen parent,
		@NotNull BooleanConsumer callback,
		@NotNull Component title,
		@NotNull Component message,
		@NotNull Component yesLabel,
		@NotNull Style yesStyle,
		@NotNull Component noLabel
	) {
		super(parent, title);
		this.callback = callback;
		this.message = message;
		this.yesLabel = yesLabel;
		this.yesStyle = yesStyle;
		this.noLabel = noLabel;
		useCenter();
	}
	@Override
	protected void buildWidgets() {
		yesButton = addRenderableWidget(new OreButton(0, yesLabel, yesStyle, b -> callback.accept(true)));
		noButton = addRenderableWidget(new OreButton(0, noLabel, Style.SECONDARY, b -> callback.accept(false)));
	}
	@Override
	protected int contentWidth() {
		return WIDTH;
	}
	/** 按钮放进底栏（实色带），不占正文高度。 */
	@Override
	protected boolean hasFooter() {
		return true;
	}
	@Override
	protected int contentHeight() {
		lines = font.split(message, WIDTH);
		return lines.size() * (font.lineHeight + LINE_GAP) + MESSAGE_GAP;
	}
	/** 两个按钮**竖排、占满整行宽**：肯定在上（Primary/Destructive）、否定在下（Secondary）。 */
	@Override
	protected void layoutContent() {
		if (yesButton == null) return;   // 首次 relayout 发生在 buildWidgets 之前
		var buttonW = panelWidth - BUTTON_MARGIN * 2;
		var buttonH = OreButton.preferredHeight();
		var blockH = buttonH * 2 + BUTTON_GAP;
		var x = panelX + BUTTON_MARGIN;
		var y = panelY + panelHeight - 1 - FOOTER_H + (FOOTER_H - blockH) / 2;
		yesButton.setRectangle(buttonW, buttonH, x, y);
		noButton.setRectangle(buttonW, buttonH, x, y + buttonH + BUTTON_GAP);
	}
	@Override
	protected void renderContent(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		var y = contentY();
		for (var line : lines) {
			gui.drawString(font, line, contentX() + (contentW() - font.width(line)) / 2, y, TEXT_COLOR, true);
			y += font.lineHeight + LINE_GAP;
		}
	}
}
