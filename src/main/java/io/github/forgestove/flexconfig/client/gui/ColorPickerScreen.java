package io.github.forgestove.flexconfig.client.gui;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.forgestove.flexconfig.client.ClientUtil;
import io.github.forgestove.flexconfig.client.Translation;
import io.github.forgestove.flexconfig.client.gui.im.core.*;
import io.github.forgestove.flexconfig.client.gui.im.core.UiFocus.Target;
import io.github.forgestove.flexconfig.client.gui.im.widget.*;
import io.github.forgestove.flexconfig.client.gui.im.widget.UiButton.Style;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.IntConsumer;
import java.util.regex.Pattern;
/** 颜色选择器。 */
public class ColorPickerScreen extends Screen {
	private static final int PICKER_SIZE = 128, BAR_WIDTH = 16, PAD = 8, ACTION_WIDTH = 64, ACTION_HEIGHT = 18, HEX_WIDTH = 70;
	private static final Pattern HEX_PATTERN = Pattern.compile("[0-9A-Fa-f]*");
	private final Screen parent;
	private final boolean hasAlpha;
	private final UiContext ui = new UiContext();
	private final UiTextField hex;
	private final UiButton ok, cancel;
	private final int[] sbPixels = new int[PICKER_SIZE * PICKER_SIZE];
	private float hue, saturation = 1F, brightness = 1F;
	private int alpha = 255;
	private int dragMode;
	private int zoom = 1, logicalWidth, logicalHeight;
	private UiControl pressedControl;
	private UiBounds panel = UiBounds.EMPTY, sv = UiBounds.EMPTY, hueBar = UiBounds.EMPTY, alphaBar = UiBounds.EMPTY;
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
		ok = new UiButton(Component.translatable("gui.ok"), Style.PRIMARY, () -> {
			onColorSelected.accept(colorFromHSB());
			ClientUtil.mc.setScreen(parent);
		});
		cancel = new UiButton(Translation.BACK_LABEL, Style.SECONDARY, () -> ClientUtil.mc.setScreen(parent));
		rebuildSBCache();
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
	@Override
	@SuppressWarnings({"DuplicatedCode", "ExtractMethod"})
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		renderBackground(graphics, mouseX, mouseY, delta);
		ui.onRefresh(() -> {});
		zoom = UiPrefs.zoom(width, height);
		logicalWidth = Math.max(1, width / zoom);
		logicalHeight = Math.max(1, height / zoom);
		ui.beginFrame(graphics, ClientUtil.mc.getTimer().getRealtimeDeltaTicks() / 20F, mouseX / (double) zoom, mouseY / (double) zoom);
		ui.viewport(logicalWidth, logicalHeight);
		layout();
		var painter = painter();
		painter.rect(0, 0, width, height, UiTheme.BACKDROP_TOP);
		painter.pushScale(zoom);
		painter.rect(panel.x() + 4, panel.y() + 4, panel.width(), panel.height(), UiTheme.SHADOW);
		painter.rect(panel.x() + 2, panel.y() + 2, panel.width(), panel.height(), UiTheme.SHADOW);
		painter.border(panel.x() - 1, panel.y() - 1, panel.width() + 2, panel.height() + 2, UiTheme.WINDOW_EDGE);
		var titleBar = new UiBounds(panel.x(), panel.y(), panel.width(), UiTheme.HEADER_HEIGHT);
		painter.rect(titleBar.x(), titleBar.y(), titleBar.width(), titleBar.height() - 3, UiTheme.TITLEBAR);
		painter.rect(titleBar.x(), titleBar.bottom() - 3, titleBar.width(), 1, UiTheme.TITLEBAR_EDGE);
		painter.rect(titleBar.x(), titleBar.bottom() - 1, titleBar.width(), 1, UiTheme.WINDOW_EDGE);
		painter.rect(panel.x(), titleBar.bottom(), panel.width(), panel.height() - titleBar.height(), UiTheme.PANEL);
		var heading = title.getString().toUpperCase(Locale.ROOT);
		painter.text(heading, titleBar.x() + (titleBar.width() - painter.textWidth(heading)) / 2, titleBar.y() + (titleBar.height() - 3 - 10 + 1) / 2, UiTheme.TITLE_INK, true);
		renderSBSquare(painter);
		renderHueBar(painter);
		if (hasAlpha) renderAlphaBar(painter);
		renderSelectors(painter);
		hex.setBounds(new UiBounds(panel.x() + PAD, sv.bottom() + PAD, HEX_WIDTH, ACTION_HEIGHT));
		ok.setBounds(new UiBounds(panel.right() - PAD - ACTION_WIDTH, hex.bounds().y(), ACTION_WIDTH, ACTION_HEIGHT));
		cancel.setBounds(new UiBounds(ok.bounds().x() - UiTheme.GAP - ACTION_WIDTH, hex.bounds().y(), ACTION_WIDTH, ACTION_HEIGHT));
		var focusables = new ArrayList<Target>();
		hex.render(ui);
		ok.render(ui);
		cancel.render(ui);
		focusables.add(hex);
		focusables.add(ok);
		focusables.add(cancel);
		ui.focus.setOrder(focusables);
		var current = ui.focus.current();
		if (ui.focusVisible() && current instanceof UiControl control && !(control instanceof UiTextField)) painter.border(control.focusBounds().inset(-1), ui.accent().light());
		ui.renderTooltip();
		painter.popScale();
	}
	private UiPainter painter() {
		return ui.painter;
	}
	private void layout() {
		var width = PAD + PICKER_SIZE + PAD + BAR_WIDTH + (hasAlpha ? PAD + BAR_WIDTH : 0) + PAD;
		var height = UiTheme.HEADER_HEIGHT + PAD + PICKER_SIZE + PAD + ACTION_HEIGHT + PAD;
		panel = new UiBounds((logicalWidth - width) / 2, (logicalHeight - height) / 2, width, height);
		var top = panel.y() + UiTheme.HEADER_HEIGHT + PAD;
		sv = new UiBounds(panel.x() + PAD, top, PICKER_SIZE, PICKER_SIZE);
		hueBar = new UiBounds(sv.right() + PAD, top, BAR_WIDTH, PICKER_SIZE);
		alphaBar = hasAlpha ? new UiBounds(hueBar.right() + PAD, top, BAR_WIDTH, PICKER_SIZE) : UiBounds.EMPTY;
	}
	private void renderSBSquare(UiPainter painter) {
		var block = 4;
		for (var by = 0; by < PICKER_SIZE; by += block)
			for (var bx = 0; bx < PICKER_SIZE; bx += block) {
				var index = by * PICKER_SIZE + bx;
				var color = sbPixels[index];
				painter.rect(sv.x() + bx, sv.y() + by, Math.min(block, PICKER_SIZE - bx), Math.min(block, PICKER_SIZE - by), color);
			}
		painter.border(sv, UiTheme.WINDOW_EDGE);
	}
	private void renderHueBar(UiPainter painter) {
		var step = 4;
		for (var dy = 0; dy < PICKER_SIZE; dy += step) {
			var color = 0xFF000000 | Color.HSBtoRGB((float) dy / (PICKER_SIZE - 1), 1F, 1F) & 0xFFFFFF;
			painter.rect(hueBar.x(), hueBar.y() + dy, BAR_WIDTH, Math.min(step, PICKER_SIZE - dy), color);
		}
		painter.border(hueBar, UiTheme.WINDOW_EDGE);
	}
	private void renderAlphaBar(UiPainter painter) {
		var base = colorFromHSB() & 0xFFFFFF;
		var step = 4;
		for (var dy = 0; dy < PICKER_SIZE; dy += step) {
			var a = 255 - dy * 255 / (PICKER_SIZE - 1);
			painter.rect(alphaBar.x(), alphaBar.y() + dy, BAR_WIDTH, Math.min(step, PICKER_SIZE - dy), a << 24 | base);
		}
		painter.border(alphaBar, UiTheme.WINDOW_EDGE);
	}
	private void renderSelectors(UiPainter painter) {
		var sx = sv.x() + (int) (saturation * (PICKER_SIZE - 1));
		var sy = sv.y() + (int) ((1F - brightness) * (PICKER_SIZE - 1));
		painter.border(sx - 4, sy - 4, 9, 9, 0xFFFFFFFF);
		painter.border(sx - 3, sy - 3, 7, 7, UiTheme.WINDOW_EDGE);
		var hueY = sv.y() + (int) (hue * (PICKER_SIZE - 1));
		painter.rect(hueBar.x() - 3, hueY - 1, BAR_WIDTH + 6, 1, 0xFFFFFFFF);
		if (!hasAlpha) return;
		var alphaY = sv.y() + (int) ((1F - alpha / 255F) * (PICKER_SIZE - 1));
		painter.rect(alphaBar.x() - 3, alphaY - 1, BAR_WIDTH + 6, 1, 0xFFFFFFFF);
	}
	private void rebuildSBCache() {
		for (var y = 0; y < PICKER_SIZE; y++) {
			var b = 1F - (float) y / (PICKER_SIZE - 1);
			for (var x = 0; x < PICKER_SIZE; x++) {
				var s = (float) x / (PICKER_SIZE - 1);
				sbPixels[y * PICKER_SIZE + x] = 0xFF000000 | Color.HSBtoRGB(hue, s, b) & 0xFFFFFF;
			}
		}
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		mouseX /= zoom;
		mouseY /= zoom;
		ui.setFocusVisible(false);
		ui.pointer(mouseX, mouseY);
		if (button != 0) return false;
		if (ok.mouseClicked(ui, mouseX, mouseY, button)) {
			pressedControl = ok;
			return true;
		}
		if (cancel.mouseClicked(ui, mouseX, mouseY, button)) {
			pressedControl = cancel;
			return true;
		}
		if (hex.mouseClicked(ui, mouseX, mouseY, button)) return true;
		if (sv.contains(mouseX, mouseY)) {
			dragMode = 1;
			applyDrag(mouseX, mouseY);
			return true;
		}
		if (hueBar.contains(mouseX, mouseY)) {
			dragMode = 2;
			applyDrag(mouseX, mouseY);
			return true;
		}
		if (hasAlpha && alphaBar.contains(mouseX, mouseY)) {
			dragMode = 3;
			applyDrag(mouseX, mouseY);
			return true;
		}
		return false;
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		mouseX /= zoom;
		mouseY /= zoom;
		ui.pointer(mouseX, mouseY);
		return applyDrag(mouseX, mouseY);
	}
	private boolean applyDrag(double mouseX, double mouseY) {
		switch (dragMode) {
			case 1 -> {
				saturation = Mth.clamp((float) (mouseX - sv.x()) / (PICKER_SIZE - 1), 0F, 1F);
				brightness = Mth.clamp(1F - (float) (mouseY - sv.y()) / (PICKER_SIZE - 1), 0F, 1F);
			}
			case 2 -> {
				var oldHue = hue;
				hue = Mth.clamp((float) (mouseY - hueBar.y()) / (PICKER_SIZE - 1), 0F, 1F);
				if (oldHue != hue) rebuildSBCache();
			}
			case 3 -> alpha = Mth.clamp((int) (255F * (1F - (float) (mouseY - alphaBar.y()) / (PICKER_SIZE - 1))), 0, 255);
			default -> {
				return false;
			}
		}
		hex.setValue(hexOf(colorFromHSB()));
		return true;
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		mouseX /= zoom;
		mouseY /= zoom;
		if (pressedControl != null) {
			var control = pressedControl;
			pressedControl = null;
			ui.pointer(mouseX, mouseY);
			control.mouseReleased(ui, mouseX, mouseY, button);
			return true;
		}
		dragMode = 0;
		return true;
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
		if (hex.keyPressed(ui, keyCode, scanCode, modifiers) || ok.keyPressed(ui, keyCode, scanCode, modifiers) || cancel.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		return hex.charTyped(ui, codePoint);
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
