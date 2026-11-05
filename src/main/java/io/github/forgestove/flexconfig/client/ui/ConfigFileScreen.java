package io.github.forgestove.flexconfig.client.ui;
import io.github.forgestove.flexconfig.ConfigHandler;
import io.github.forgestove.flexconfig.client.*;
import io.github.forgestove.flexconfig.client.ui.core.*;
import io.github.forgestove.flexconfig.client.ui.core.UiFocus.Target;
import io.github.forgestove.flexconfig.client.ui.widget.*;
import io.github.forgestove.flexconfig.client.ui.widget.UiButton.Style;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;
import org.lwjgl.glfw.GLFW;

import java.util.*;
/** 单份配置的模组不经过本界面，由 {@link FlexConfigScreenFactory} 直接打开 {@link ConfigScreen}。 */
public class ConfigFileScreen extends Screen {
	private static final int BUTTON_HEIGHT = 20, SPACING = 6;
	private final String modId;
	private final List<ConfigHandler<?, ?>> handlers;
	private final @Nullable Screen parent;
	private final List<UiButton> fileButtons = new ArrayList<>();
	private UiButton backButton;
	private final UiContext ui = new UiContext();
	private UiControl pressedControl;
	private int zoom = 1, logicalWidth, logicalHeight;
	private UiBounds panel = UiBounds.EMPTY;
	public ConfigFileScreen(String modId, List<ConfigHandler<?, ?>> handlers) {
		super(ClientUtil.modName(modId));
		this.modId = modId;
		this.handlers = handlers;
		parent = ClientUtil.mc.screen;
	}
	@Override
	protected void init() {
		fileButtons.clear();
		if (!handlers.isEmpty()) ui.setAccent(handlers.get(0).getAccent());
		for (var handler : handlers) {
			var available = FlexConfigScreenFactory.available(handler);
			var button = new UiButton(label(handler), Style.SECONDARY, () -> open(handler));
			button.setEnabled(available);
			if (!available) button.tooltip(Translation.tooltip(Translation.SERVER_CONFIG_UNAVAILABLE, Translation.SERVER_CONFIG_UNAVAILABLE_TOOLTIP));
			fileButtons.add(button);
		}
		backButton = new UiButton(Translation.BACK_LABEL, Style.SECONDARY, this::onClose);
		zoom = UiPrefs.zoom(width, height);
		logicalWidth = Math.max(1, width / zoom);
		logicalHeight = Math.max(1, height / zoom);
		updatePanel();
	}
	private void updatePanel() {
		var width = 300;
		var height = 22 + 12 + Math.max(1, fileButtons.size()) * (BUTTON_HEIGHT + SPACING) + BUTTON_HEIGHT + 16;
		panel = new UiBounds((logicalWidth - width) / 2, (logicalHeight - height) / 2, width, height);
	}
	private Component label(ConfigHandler<?, ?> handler) {
		var key = "%s.config.file.%s".formatted(modId, handler.getType().extension());
		return Component.translatableWithFallback(key, handler.getFileName());
	}
	private void open(ConfigHandler<?, ?> handler) {
		ClientUtil.mc.setScreen(new ConfigScreen<>(handler));
	}
	@Override
	@SuppressWarnings("DuplicatedCode")
	public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		ui.onRefresh(() -> {});
		ui.beginFrame(graphics, ClientUtil.mc.getTimer().getRealtimeDeltaTicks() / 20F, mouseX / (double) zoom, mouseY / (double) zoom);
		ui.viewport(logicalWidth, logicalHeight);
		renderBackground(graphics, mouseX, mouseY, delta);
		updatePanel();
		var painter = ui.painter;
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
		var focusables = new ArrayList<Target>();
		var y = titleBar.bottom() + 12;
		for (var button : fileButtons) {
			button.setBounds(new UiBounds(panel.x() + 12, y, panel.width() - 24, BUTTON_HEIGHT));
			button.render(ui);
			if (button.focusable()) focusables.add(button);
			y += BUTTON_HEIGHT + SPACING;
		}
		backButton.setBounds(new UiBounds(panel.x() + 12, panel.bottom() - 12 - BUTTON_HEIGHT, panel.width() - 24, BUTTON_HEIGHT));
		backButton.render(ui);
		focusables.add(backButton);
		ui.focus.setOrder(focusables);
		var current = ui.focus.current();
		if (ui.focusVisible() && current instanceof UiControl control) painter.border(control.focusBounds().inset(-1), ui.accent().light());
		ui.renderTooltip();
		painter.popScale();
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		mouseX /= zoom;
		mouseY /= zoom;
		ui.setFocusVisible(false);
		ui.pointer(mouseX, mouseY);
		for (var fileButton : fileButtons) if (fileButton.mouseClicked(ui, mouseX, mouseY, button)) {
			pressedControl = fileButton;
			return true;
		}
		if (backButton.mouseClicked(ui, mouseX, mouseY, button)) {
			pressedControl = backButton;
			return true;
		}
		return false;
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		mouseX /= zoom;
		mouseY /= zoom;
		if (pressedControl == null) return false;
		var control = pressedControl;
		pressedControl = null;
		ui.pointer(mouseX, mouseY);
		control.mouseReleased(ui, mouseX, mouseY, button);
		return true;
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
			onClose();
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_TAB) {
			ui.setFocusVisible(true);
			ui.focus.move(hasShiftDown() ? -1 : 1);
			return true;
		}
		for (var fileButton : fileButtons) if (fileButton.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		return backButton.keyPressed(ui, keyCode, scanCode, modifiers);
	}
	@Override
	public void onClose() {
		ClientUtil.mc.setScreen(parent);
	}
}
