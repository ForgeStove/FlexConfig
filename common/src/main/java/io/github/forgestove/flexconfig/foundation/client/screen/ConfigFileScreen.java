package io.github.forgestove.flexconfig.foundation.client.screen;
import io.github.forgestove.flexconfig.api.menu.Accent;
import io.github.forgestove.flexconfig.api.registration.ConfigHandler;
import io.github.forgestove.flexconfig.foundation.client.ClientUtil;
import io.github.forgestove.flexconfig.foundation.ui.*;
import io.github.forgestove.flexconfig.foundation.ui.core.UiText;
import net.minecraft.client.gui.screens.Screen;

import java.util.List;
import java.util.function.IntConsumer;
public class ConfigFileScreen extends MinecraftScreen implements ConfigActions {
	private final Screen parent;
	private final List<ConfigHandler<?, ?>> handlers;
	public ConfigFileScreen(String modId, List<ConfigHandler<?, ?>> handlers) {
		super(UiText.from(ClientUtil.modName(modId)));
		parent = ClientUtil.mc.screen;
		this.handlers = handlers;
		setView(new ConfigFileView(this, uiTitle(), modId, handlers));
	}
	@Override
	public int guiWidth() {
		return width;
	}
	@Override
	public int guiHeight() {
		return height;
	}
	@Override
	public boolean shiftDown() {
		return hasShiftDown();
	}
	@Override
	public void close() {
		ClientUtil.mc.setScreen(parent);
	}
	@Override
	public Accent accent() {
		return handlers.isEmpty() ? Accent.DEFAULT : handlers.get(0).getAccent();
	}
	@Override
	public void openConfig(ConfigHandler<?, ?> handler) {
		ClientUtil.mc.setScreen(new ConfigScreen<>(handler));
	}
	@Override
	public void openColorPicker(int color, boolean alpha, IntConsumer onSelected) {
		ClientUtil.mc.setScreen(new ColorPickerScreen(this, color, alpha, onSelected));
	}
	@Override
	public void copyToClipboard(String text) {
		ClientUtil.mc.keyboardHandler.setClipboard(text);
	}
	@Override
	public boolean inWorld() {
		return ClientUtil.mc.level != null;
	}
	@Override
	public boolean localServer() {
		return ClientUtil.mc.isLocalServer();
	}
	@Override
	public void disconnectGame() {
		ClientUtil.mc.disconnect();
	}
	@Override
	public void stopGame() {
		ClientUtil.mc.stop();
	}
}
