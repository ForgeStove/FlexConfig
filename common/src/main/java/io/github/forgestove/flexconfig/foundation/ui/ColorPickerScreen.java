package io.github.forgestove.flexconfig.foundation.ui;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.forgestove.flexconfig.foundation.client.*;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.core.UiFocus.Target;
import io.github.forgestove.flexconfig.foundation.ui.screen.UiScreen;
import io.github.forgestove.flexconfig.foundation.ui.widget.*;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiButton.Style;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor.ARGB32;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.*;
import java.util.function.IntConsumer;
import java.util.regex.Pattern;
/** 颜色选择器：顶部预览，下方色相、饱和度、亮度、透明度通道滑条与十六进制输入。 */
public class ColorPickerScreen extends UiScreen {
	private static final int PREVIEW_SIZE = 80, COLUMN_WIDTH = 148, PAD = 8;
	private static final int ACTION_WIDTH = 64, ACTION_HEIGHT = 18, HEX_WIDTH = 70;
	private static final Pattern HEX_PATTERN = Pattern.compile("[0-9A-Fa-f]*");
	/** 色相六段，末段接回红。 */
	private static final int[] HUE_STOPS = {0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000};
	private final Screen parent;
	private final boolean hasAlpha;
	private final List<UiColorSlider> sliders = new ArrayList<>();
	private final UiTextField hex;
	private final UiButton ok, cancel;
	private float hue, saturation = 1F, brightness = 1F;
	private int alpha = 255;
	private UiBounds panel = UiBounds.EMPTY, preview = UiBounds.EMPTY;
	public ColorPickerScreen(Screen parent, int initialColor, boolean hasAlpha, IntConsumer onColorSelected) {
		super(Translation.COLOR_PICKER_TOOLTIP);
		this.parent = parent;
		if (parent instanceof ConfigScreen<?, ?> screen) ui.setAccent(screen.accent());
		this.hasAlpha = hasAlpha;
		updateHSBFromColor(initialColor);
		hex = new UiTextField(hexOf(colorFromHSB()));
		hex.setMaxLength(hasAlpha ? 8 : 6);
		hex.setFilter(s -> HEX_PATTERN.matcher(s).matches());
		hex.setResponder(this::onHexInput);
		sliders.add(new UiColorSlider(
			() -> hue, value -> {
			hue = (float) value;
			syncHex();
		}, this::hueTrack
		));
		sliders.add(new UiColorSlider(
			() -> saturation, value -> {
			saturation = (float) value;
			syncHex();
		}, this::saturationTrack
		));
		sliders.add(new UiColorSlider(
			() -> brightness, value -> {
			brightness = (float) value;
			syncHex();
		}, this::brightnessTrack
		));
		if (hasAlpha) sliders.add(new UiColorSlider(
			() -> alpha / 255D, value -> {
			alpha = Math.round((float) value * 255F) & 0xFF;
			syncHex();
		}, this::alphaTrack
		));
		ok = new UiButton(
			Component.translatable("gui.ok").withStyle(ChatFormatting.BOLD), Style.PRIMARY, () -> {
			onColorSelected.accept(colorFromHSB());
			ClientUtil.mc.setScreen(parent);
		}
		);
		cancel = new UiButton(Translation.BACK_LABEL, Style.SECONDARY, () -> ClientUtil.mc.setScreen(parent));
	}
	private void updateHSBFromColor(int color) {
		if (hasAlpha) alpha = ARGB32.alpha(color);
		var hsb = Color.RGBtoHSB(ARGB32.red(color), ARGB32.green(color), ARGB32.blue(color), null);
		hue = hsb[0];
		saturation = hsb[1];
		brightness = hsb[2];
	}
	private int colorFromHSB() {
		var rgb = Color.HSBtoRGB(hue, saturation, brightness) & 0xFFFFFF;
		return hasAlpha ? alpha << 24 | rgb : rgb;
	}
	private String hexOf(int color) {
		return String.format(hasAlpha ? "%08X" : "%06X", color);
	}
	private void onHexInput(String text) {
		if (text.length() != (hasAlpha ? 8 : 6)) return;
		try {
			updateHSBFromColor(Integer.parseUnsignedInt(text, 16));
		} catch (NumberFormatException ignored) {}
	}
	private void syncHex() {
		hex.setValue(hexOf(colorFromHSB()));
	}
	@Override
	protected void renderBeforeScale(int mouseX, int mouseY, float delta) {
		renderBackground(ui.graphics, mouseX, mouseY, delta);
	}
	@Override
	protected void renderUi(UiContext ui, int mouseX, int mouseY, float delta) {
		ui.onRefresh(() -> {});
		layout();
		var painter = painter();
		painter.rect(0, 0, logicalWidth, logicalHeight, UiTheme.BACKDROP_TOP);
		painter.rect(panel.x() + 4, panel.y() + 4, panel.width(), panel.height(), UiTheme.SHADOW);
		painter.rect(panel.x() + 2, panel.y() + 2, panel.width(), panel.height(), UiTheme.SHADOW);
		painter.border(panel.x() - 1, panel.y() - 1, panel.width() + 2, panel.height() + 2, UiTheme.WINDOW_EDGE);
		var titleBar = new UiBounds(panel.x(), panel.y(), panel.width(), UiTheme.HEADER_HEIGHT);
		painter.rect(titleBar.x(), titleBar.y(), titleBar.width(), titleBar.height() - 2, UiTheme.TITLEBAR);
		painter.rect(titleBar.x(), titleBar.bottom() - 2, titleBar.width(), 1, UiTheme.TITLEBAR_EDGE);
		painter.rect(titleBar.x(), titleBar.bottom() - 1, titleBar.width(), 1, UiTheme.WINDOW_EDGE);
		painter.rect(panel.x(), titleBar.bottom(), panel.width(), panel.height() - titleBar.height(), UiTheme.PANEL);
		var heading = title.getString().toUpperCase(Locale.ROOT);
		painter.textEmphasis(
			heading,
			titleBar.x() + (titleBar.width() - painter.textWidthEmphasis(heading)) / 2,
			titleBar.y() + (titleBar.height() - 3 - 10 + 1) / 2,
			UiTheme.TITLE_INK
		);
		renderPreview();
		for (var slider : sliders) slider.render(ui);
		hex.render(ui);
		ok.render(ui);
		cancel.render(ui);
		var focusables = new ArrayList<Target>(sliders);
		focusables.add(hex);
		focusables.add(ok);
		focusables.add(cancel);
		ui.focus.setOrder(focusables);
		renderFocusRing(painter, control -> !(control instanceof UiTextField));
	}
	private UiPainter painter() {
		return ui.painter;
	}
	private void layout() {
		var rowPitch = UiTheme.CONTROL_HEIGHT + UiTheme.GAP;
		var rowsHeight = sliders.size() * rowPitch - UiTheme.GAP;
		var width = COLUMN_WIDTH + PAD * 2;
		var height = UiTheme.HEADER_HEIGHT
			+ PAD
			+ PREVIEW_SIZE
			+ UiTheme.GAP
			+ rowsHeight
			+ UiTheme.GAP
			+ UiTheme.CONTROL_HEIGHT
			+ UiTheme.GAP
			+ ACTION_HEIGHT
			+ PAD;
		panel = new UiBounds((logicalWidth - width) / 2, (logicalHeight - height) / 2, width, height);
		var top = panel.y() + UiTheme.HEADER_HEIGHT + PAD;
		preview = new UiBounds(panel.x() + (panel.width() - PREVIEW_SIZE) / 2, top, PREVIEW_SIZE, PREVIEW_SIZE);
		var rowX = panel.x() + (panel.width() - COLUMN_WIDTH) / 2;
		var rowY = preview.bottom() + UiTheme.GAP;
		for (var slider : sliders) {
			slider.setBounds(new UiBounds(rowX, rowY, COLUMN_WIDTH, UiTheme.CONTROL_HEIGHT));
			rowY += rowPitch;
		}
		hex.setBounds(new UiBounds(panel.x() + (panel.width() - HEX_WIDTH) / 2, rowY, HEX_WIDTH, UiTheme.CONTROL_HEIGHT));
		var buttonY = hex.bounds().bottom() + UiTheme.GAP;
		var buttonsX = panel.x() + (panel.width() - (ACTION_WIDTH * 2 + UiTheme.GAP)) / 2;
		cancel.setBounds(new UiBounds(buttonsX, buttonY, ACTION_WIDTH, ACTION_HEIGHT));
		ok.setBounds(new UiBounds(buttonsX + ACTION_WIDTH + UiTheme.GAP, buttonY, ACTION_WIDTH, ACTION_HEIGHT));
	}
	private void renderPreview() {
		var painter = painter();
		painter.rect(preview, 0xFF000000 | colorFromHSB());   // 不看 alpha，避免和面板底色混合
		painter.border(preview, UiTheme.WINDOW_EDGE);
	}
	/** 色相轨道：{@link #HUE_STOPS} 的末段接回红，故只铺前六段。 */
	private void hueTrack(UiPainter painter, UiBounds bounds) {
		for (var i = 0; i < bounds.width(); i++) {
			var t = bounds.width() == 1 ? 0F : (float) i / (bounds.width() - 1);
			var segment = Math.min(HUE_STOPS.length - 2, (int) (t * 6));
			painter.rect(
				bounds.x() + i,
				bounds.y(),
				1,
				bounds.height(),
				ARGB32.lerp(t * 6 - segment, HUE_STOPS[segment], HUE_STOPS[segment + 1])
			);
		}
	}
	private void saturationTrack(UiPainter painter, UiBounds bounds) {
		horizontal(painter, bounds, argb(0F, brightness, 1F), argb(1F, brightness, 1F));
	}
	private void brightnessTrack(UiPainter painter, UiBounds bounds) {
		horizontal(painter, bounds, argb(saturation, 0F, 1F), argb(saturation, 1F, 1F));
	}
	private void alphaTrack(UiPainter painter, UiBounds bounds) {
		horizontal(painter, bounds, argb(saturation, brightness, 0F), argb(saturation, brightness, 1F));
	}
	/** 仅一格宽时无从插值，取起点色。 */
	private static void horizontal(UiPainter painter, UiBounds bounds, int from, int to) {
		for (var i = 0; i < bounds.width(); i++) {
			var t = bounds.width() == 1 ? 0F : (float) i / (bounds.width() - 1);
			painter.rect(bounds.x() + i, bounds.y(), 1, bounds.height(), ARGB32.lerp(t, from, to));
		}
	}
	private int argb(float sat, float value, float alpha) {
		var rgb = Color.HSBtoRGB(hue, sat, value) & 0xFFFFFF;
		return (Math.round(alpha * 255F) & 0xFF) << 24 | rgb;
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		var controls = new ArrayList<UiControl>(sliders);
		controls.add(hex);
		controls.add(ok);
		controls.add(cancel);
		return pressControl(controls, mouseX, mouseY, button);
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return dragControl(mouseX, mouseY, button, dragX, dragY);
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return releaseControl(mouseX, mouseY, button);
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == InputConstants.KEY_ESCAPE) {
			ClientUtil.mc.setScreen(parent);
			return true;
		}
		if (keyCode == InputConstants.KEY_TAB) {
			ui.setFocusVisible(true);
			ui.focus.move(hasShiftDown() ? -1 : 1);
			return true;
		}
		for (var slider : sliders) if (slider.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		if (hex.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		if (ok.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		if (cancel.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		if (ui.focus.is(hex)) return hex.charTyped(ui, codePoint);
		return super.charTyped(codePoint, modifiers);
	}
	@Override
	public void onClose() {
		ClientUtil.mc.setScreen(parent);
	}
	@Override
	public void resize(@NotNull Minecraft minecraft, int width, int height) {
		parent.resize(minecraft, width, height);
		super.resize(minecraft, width, height);
	}
}
