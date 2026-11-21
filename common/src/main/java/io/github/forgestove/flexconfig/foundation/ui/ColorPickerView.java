package io.github.forgestove.flexconfig.foundation.ui;
import io.github.forgestove.flexconfig.foundation.client.Translation;
import io.github.forgestove.flexconfig.foundation.ui.ConfigInput.*;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.core.UiFocus.Target;
import io.github.forgestove.flexconfig.foundation.ui.screen.UiScreenView;
import io.github.forgestove.flexconfig.foundation.ui.widget.*;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiButton.Style;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.*;
import java.util.function.IntConsumer;
import java.util.regex.Pattern;
/** 颜色选择器：顶部预览，下方色相、饱和度、亮度、透明度通道滑条与十六进制输入。 */
public class ColorPickerView extends UiScreenView {
	private static final int PREVIEW_SIZE = 80, COLUMN_WIDTH = 148, PAD = 8;
	private static final int ACTION_WIDTH = 64, ACTION_HEIGHT = 18, HEX_WIDTH = 70;
	private static final Pattern HEX_PATTERN = Pattern.compile("[0-9A-Fa-f]*");
	/** 色相六段，末段接回红。 */
	private static final int[] HUE_STOPS = {0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000};
	private final boolean hasAlpha;
	private final List<UiColorSlider> sliders = new ArrayList<>();
	private final UiTextField hex;
	private final UiButton ok, cancel;
	private float hue, saturation = 1F, brightness = 1F;
	private int alpha = 255;
	private UiBounds panel = UiBounds.EMPTY, preview = UiBounds.EMPTY;
	public ColorPickerView(ConfigHost host, int initialColor, boolean hasAlpha, IntConsumer onColorSelected) {
		super(host, Translation.COLOR_PICKER_TOOLTIP);
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
			UiText.translatable("gui.ok").withStyle(UiStyle.BOLD), Style.PRIMARY, () -> {
			onColorSelected.accept(colorFromHSB());
			host.close();
		}
		);
		cancel = new UiButton(Translation.BACK_LABEL, Style.SECONDARY, host::close);
	}
	/** 仅一格宽时无从插值，取起点色。 */
	private static void horizontal(UiPainter painter, UiBounds bounds, int from, int to) {
		for (var i = 0; i < bounds.width(); i++) {
			var t = bounds.width() == 1 ? 0F : (float) i / (bounds.width() - 1);
			painter.rect(bounds.x() + i, bounds.y(), 1, bounds.height(), lerp(t, from, to));
		}
	}
	private static int alphaOf(int color) {
		return color >>> 24;
	}
	private static int redOf(int color) {
		return color >> 16 & 0xFF;
	}
	private static int greenOf(int color) {
		return color >> 8 & 0xFF;
	}
	private static int blueOf(int color) {
		return color & 0xFF;
	}
	private static int lerp(float t, int from, int to) {
		return alphaOf(from) + Math.round((alphaOf(to) - alphaOf(from)) * t) << 24
			| redOf(from) + Math.round((redOf(to) - redOf(from)) * t) << 16
			| greenOf(from) + Math.round((greenOf(to) - greenOf(from)) * t) << 8
			| blueOf(from) + Math.round((blueOf(to) - blueOf(from)) * t);
	}
	@Override
	public void init() {
		ui.setAccent(host.accent());
	}
	@Override
	public boolean input(ConfigInput event) {
		return switch (event) {
			case MouseDown e -> pressControl(controls(), e.x(), e.y(), e.button());
			case MouseUp e -> releaseControl(e.x(), e.y(), e.button());
			case MouseDrag e -> dragControl(e.x(), e.y(), e.button(), e.dragX(), e.dragY());
			case Key e -> key(e.keyCode(), e.scanCode(), e.modifiers());
			case Char e -> ui.focus.is(hex) && hex.charTyped(ui, e.codePoint());
			case Scroll ignored -> false;
		};
	}
	@Override
	public void onClose() {
		host.close();
	}
	@Override
	public void onRemoved() {}
	@Override
	public void refresh() {}
	@Override
	public boolean closed() {
		return false;
	}
	private void updateHSBFromColor(int color) {
		if (hasAlpha) alpha = alphaOf(color);
		var hsb = Color.RGBtoHSB(redOf(color), greenOf(color), blueOf(color), null);
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
	protected void renderUi(UiContext ui, int mouseX, int mouseY, float seconds) {
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
		var heading = title().getString().toUpperCase(Locale.ROOT);
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
			painter.rect(bounds.x() + i, bounds.y(), 1, bounds.height(), lerp(t * 6 - segment, HUE_STOPS[segment],
				HUE_STOPS[segment + 1]));
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
	private int argb(float sat, float value, float alpha) {
		var rgb = Color.HSBtoRGB(hue, sat, value) & 0xFFFFFF;
		return (Math.round(alpha * 255F) & 0xFF) << 24 | rgb;
	}
	private List<UiControl> controls() {
		var controls = new ArrayList<UiControl>(sliders);
		controls.add(hex);
		controls.add(ok);
		controls.add(cancel);
		return controls;
	}
	private boolean key(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			host.close();
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_TAB) {
			ui.setFocusVisible(true);
			ui.focus.move(host.shiftDown() ? -1 : 1);
			return true;
		}
		for (var slider : sliders) if (slider.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		if (hex.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		if (ok.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		return cancel.keyPressed(ui, keyCode, scanCode, modifiers);
	}
}
