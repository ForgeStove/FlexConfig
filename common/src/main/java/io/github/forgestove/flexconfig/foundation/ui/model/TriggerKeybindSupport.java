package io.github.forgestove.flexconfig.foundation.ui.model;
import io.github.forgestove.flexconfig.foundation.tree.ValueConfigNode;
import io.github.forgestove.flexconfig.foundation.ui.factory.*;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
public class TriggerKeybindSupport implements CaptureTarget {
	private final ValueConfigNode node;
	private final RowContext ctx;
	private final @Nullable String initialSerialized;
	private boolean capturing;
	public TriggerKeybindSupport(ValueConfigNode node, RowContext ctx) {
		this.node = node;
		this.ctx = ctx;
		initialSerialized = ctx.handler().getTriggerKeybind(node.getPath());
	}
	public void register(Runnable valueChanger) {
		var handler = ctx.handler();
		var path = node.getPath();
		var serialized = handler.getTriggerKeybind(path);
		handler.setTriggerKeybind(
			path, serialized, () -> {
				valueChanger.run();
				writeEditing();
				ctx.saveByKeybind();
			}
		);
	}
	private void writeEditing() {
		node.writeEditingToConfig(ctx.config());
	}
	public boolean capturing() {
		return capturing;
	}
	public void beginCapture() {
		capturing = true;
		ctx.beginCapture(this);
		ctx.refresh();
	}
	private void finishCapture() {
		capturing = false;
		ctx.endCapture(this);
		ctx.refresh();
	}
	public EntryKeybind current() {
		var serialized = ctx.handler().getTriggerKeybind(node.getPath());
		return serialized != null ? EntryKeybind.deserialize(serialized) : EntryKeybind.UNBOUND;
	}
	public @Nullable String serialized() {
		return ctx.handler().getTriggerKeybind(node.getPath());
	}
	public boolean changed() {
		return !Objects.equals(serialized(), initialSerialized);
	}
	@Override
	public boolean captureKey(int keyCode) {
		if (!capturing) return false;
		if (Keybinds.support().isModifier(keyCode)) return true;
		var handler = ctx.handler();
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) handler.setTriggerKeybind(node.getPath(), null, null);
		else handler.setTriggerKeybind(node.getPath(), Keybinds.support().captureKey(keyCode).serialize(), null);
		finishCapture();
		return true;
	}
	@Override
	public void captureMouse(int button) {
		if (!capturing) return;
		ctx.handler().setTriggerKeybind(node.getPath(), Keybinds.support().captureMouse(button).serialize(), null);
		finishCapture();
	}
	public void resetToDefault() {
		ctx.handler().setTriggerKeybind(node.getPath(), null, null);
	}
	public void resetToActive() {
		ctx.handler().setTriggerKeybind(node.getPath(), initialSerialized, null);
	}
}
