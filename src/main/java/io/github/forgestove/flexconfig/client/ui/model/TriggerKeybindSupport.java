package io.github.forgestove.flexconfig.client.ui.model;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;
import io.github.forgestove.flexconfig.client.ui.factory.EntryKeybind;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import org.jetbrains.annotations.Nullable;
import java.util.Objects;
/** 可捕获行（Boolean/Enum）的触发键位：注册、捕获、重置与撤销快照。 */
public class TriggerKeybindSupport implements CaptureTarget {
	private final ValueConfigNode<?, ?> node;
	private final RowContext ctx;
	private final @Nullable String initialSerialized;
	private boolean capturing;
	public TriggerKeybindSupport(ValueConfigNode<?, ?> node, RowContext ctx) {
		this.node = node;
		this.ctx = ctx;
		initialSerialized = ctx.handler().getTriggerKeybind(node.getPath());
	}
	public void register(Runnable valueChanger) {
		var handler = ctx.handler();
		var path = node.getPath();
		var serialized = handler.getTriggerKeybind(path);
		handler.setTriggerKeybind(path, serialized, () -> {
			valueChanger.run();
			writeEditing();
			ctx.saveByKeybind();
		});
	}
	@SuppressWarnings({"rawtypes", "unchecked"})
	private void writeEditing() {
		((ValueConfigNode) node).writeEditingToConfig(ctx.config());
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
		if (EntryKeybind.isModifier(keyCode)) return true;
		var handler = ctx.handler();
		if (keyCode == InputConstants.KEY_ESCAPE) handler.setTriggerKeybind(node.getPath(), null, null);
		else handler.setTriggerKeybind(
			node.getPath(),
			new EntryKeybind(Type.KEYSYM.getOrCreate(keyCode), EntryKeybind.getActiveModifierFlags()).serialize(),
			null
		);
		finishCapture();
		return true;
	}
	@Override
	public void captureMouse(int button) {
		if (!capturing) return;
		ctx.handler().setTriggerKeybind(
			node.getPath(),
			new EntryKeybind(Type.MOUSE.getOrCreate(button), EntryKeybind.getActiveModifierFlags()).serialize(),
			null
		);
		finishCapture();
	}
	public void resetToDefault() {
		ctx.handler().setTriggerKeybind(node.getPath(), null, null);
	}
	public void resetToActive() {
		ctx.handler().setTriggerKeybind(node.getPath(), initialSerialized, null);
	}
}
