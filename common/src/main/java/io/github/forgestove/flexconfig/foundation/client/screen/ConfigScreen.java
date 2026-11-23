package io.github.forgestove.flexconfig.foundation.client.screen;
import io.github.forgestove.flexconfig.api.FileKey;
import io.github.forgestove.flexconfig.api.menu.Accent;
import io.github.forgestove.flexconfig.api.registration.ConfigHandler;
import io.github.forgestove.flexconfig.foundation.client.ClientUtil;
import io.github.forgestove.flexconfig.foundation.tree.RootConfigNode;
import io.github.forgestove.flexconfig.foundation.ui.*;
import net.minecraft.client.gui.screens.Screen;

import java.util.Set;
import java.util.function.IntConsumer;
public class ConfigScreen<C, V> extends MinecraftScreen implements ConfigActions {
	public final RootConfigNode root;
	private final Screen parent;
	private final ConfigHandler<C, V> handler;
	private final ConfigShell shell;
	public ConfigScreen(ConfigHandler<C, V> handler) {
		super(handler.getConfigTree().getTitle());
		this.handler = handler;
		root = handler.getConfigTree();
		parent = ClientUtil.mc.screen;
		shell = new ConfigShell(this, handler);
		setView(shell);
	}
	public void refresh() {
		shell.refresh();
	}
	public Set<FileKey> editedFiles() {
		return Set.of(new FileKey(root.modId(), root.fileName()));
	}
	@Override
	public Accent accent() {
		return handler.getAccent();
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
		if (ClientUtil.mc.level != null) ClientUtil.mc.level.disconnect();
		ClientUtil.mc.clearLevel();
	}
	@Override
	public void stopGame() {
		ClientUtil.mc.stop();
	}
}
