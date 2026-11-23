package io.github.forgestove.flexconfig.foundation.ui;
import io.github.forgestove.flexconfig.api.client.FlexConfigScreenFactory;
import io.github.forgestove.flexconfig.api.registration.ConfigHandler;
import io.github.forgestove.flexconfig.foundation.client.Translation;
import io.github.forgestove.flexconfig.foundation.ui.ConfigInput.*;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.core.UiFocus.Target;
import io.github.forgestove.flexconfig.foundation.ui.screen.UiScreenView;
import io.github.forgestove.flexconfig.foundation.ui.widget.*;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiButton.Style;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;
public class ConfigFileView extends UiScreenView {
	private static final int BUTTON_HEIGHT = 20, SPACING = 6;
	private final ConfigActions actions;
	private final String modId;
	private final List<ConfigHandler<?, ?>> handlers;
	private final List<UiButton> fileButtons = new ArrayList<>();
	private final UiTween backdropAnim = new UiTween(0);
	private final UiTween windowAnim = new UiTween(0);
	private final UiTween riseAnim = new UiTween(UiMotion.RISE);
	private final UiTween closeAnim = new UiTween(1);
	private UiButton doneButton;
	private UiBounds panel = UiBounds.EMPTY;
	private boolean opened, closing;
	private @Nullable Runnable closeAction;
	public ConfigFileView(ConfigActions host, UiText title, String modId, List<ConfigHandler<?, ?>> handlers) {
		super(host, title);
		actions = host;
		this.modId = modId;
		this.handlers = handlers;
	}
	@Override
	public void init() {
		fileButtons.clear();
		if (!handlers.isEmpty()) ui.setAccent(handlers.get(0).getAccent());
		for (var handler : handlers) {
			var available = FlexConfigScreenFactory.available(handler);
			var button = new UiButton(label(handler), Style.SECONDARY, () -> actions.openConfig(handler));
			button.setEnabled(available);
			if (!available)
				button.tooltip(Translation.tooltip(Translation.SERVER_CONFIG_UNAVAILABLE, Translation.SERVER_CONFIG_UNAVAILABLE_TOOLTIP));
			fileButtons.add(button);
		}
		doneButton = new UiButton(Translation.DONE_LABEL.copy().withStyle(UiStyle.BOLD), Style.PRIMARY, this::onClose);
		if (!opened) {
			opened = true;
			backdropAnim.start(0, 1, UiMotion.BACKDROP_IN);
			windowAnim.start(0, 1, UiMotion.ENTER);
			riseAnim.start(UiMotion.RISE, 0, UiMotion.ENTER);
		} else {
			closing = false;
			closeAction = null;
			backdropAnim.snap(1);
			windowAnim.snap(1);
			riseAnim.snap(0);
			closeAnim.snap(1);
		}
	}
	@Override
	public boolean input(ConfigInput event) {
		if (event instanceof MouseDown e) {
			if (closing) return false;
			var controls = new ArrayList<UiControl>(fileButtons);
			controls.add(doneButton);
			return pressControl(controls, e.x(), e.y(), e.button());
		}
		if (event instanceof MouseUp e) return !closing && releaseControl(e.x(), e.y(), e.button());
		if (event instanceof Key e) return key(e.keyCode(), e.scanCode(), e.modifiers());
		return false;
	}
	@Override
	public void onClose() {
		if (closing) return;
		closing = true;
		closeAction = actions::close;
		closeAnim.start(1, 0, UiMotion.EXIT);
	}
	@Override
	public void onRemoved() {}
	@Override
	public void refresh() {}
	@Override
	public boolean closed() {
		return closing;
	}
	private void updatePanel() {
		var width = 300;
		var height = 22 + 12 + Math.max(1, fileButtons.size()) * (BUTTON_HEIGHT + SPACING) + BUTTON_HEIGHT + 16;
		panel = new UiBounds((logicalWidth - width) / 2, (logicalHeight - height) / 2, width, height);
	}
	private UiText label(ConfigHandler<?, ?> handler) {
		var key = "%s.config.file.%s".formatted(modId, handler.getType().extension());
		return UiText.translatableWithFallback(handler.getFileName(), key);
	}
	@Override
	protected void renderUi(UiContext ui, int mouseX, int mouseY, float seconds) {
		ui.onRefresh(() -> {});
		backdropAnim.update(seconds);
		windowAnim.update(seconds);
		riseAnim.update(seconds);
		closeAnim.update(seconds);
		if (closing && closeAnim.done()) {
			var action = closeAction;
			closing = false;
			closeAction = null;
			if (action != null) action.run();
			return;
		}
		updatePanel();
		var painter = ui.painter;
		var closeFactor = closeAnim.value();
		painter.rect(0, 0, logicalWidth, logicalHeight, UiTheme.mulAlpha(UiTheme.BACKDROP_TOP, backdropAnim.value() * closeFactor));
		var rise = riseAnim.value() + (closing ? (1F - closeFactor) * UiMotion.CLOSE_DROP : 0);
		painter.pushAlpha(windowAnim.value() * closeFactor);
		painter.pushTranslate(0, rise);
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
		var focusables = new ArrayList<Target>();
		var y = titleBar.bottom() + 12;
		for (var button : fileButtons) {
			button.setBounds(new UiBounds(panel.x() + 12, y, panel.width() - 24, BUTTON_HEIGHT));
			button.render(ui);
			if (button.focusable()) focusables.add(button);
			y += BUTTON_HEIGHT + SPACING;
		}
		doneButton.setBounds(new UiBounds(panel.x() + 12, panel.bottom() - 12 - BUTTON_HEIGHT, panel.width() - 24, BUTTON_HEIGHT));
		doneButton.render(ui);
		focusables.add(doneButton);
		ui.focus.setOrder(focusables);
		renderFocusRing(painter);
		painter.popTranslate();
		painter.popAlpha();
	}
	@Override
	protected boolean showTooltip() {
		return !closing;
	}
	private boolean key(int keyCode, int scanCode, int modifiers) {
		if (closing) return true;
		if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
			onClose();
			return true;
		}
		if (keyCode == GLFW.GLFW_KEY_TAB) {
			ui.setFocusVisible(true);
			ui.focus.move(host.shiftDown() ? -1 : 1);
			return true;
		}
		for (var fileButton : fileButtons) if (fileButton.keyPressed(ui, keyCode, scanCode, modifiers)) return true;
		return doneButton.keyPressed(ui, keyCode, scanCode, modifiers);
	}
}
