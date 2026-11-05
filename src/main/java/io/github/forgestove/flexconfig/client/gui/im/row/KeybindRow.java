package io.github.forgestove.flexconfig.client.gui.im.row;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import com.mojang.blaze3d.platform.InputConstants.Type;
import io.github.forgestove.flexconfig.client.gui.factory.EntryKeybind;
import io.github.forgestove.flexconfig.client.gui.factory.KeybindState;
import io.github.forgestove.flexconfig.client.gui.im.core.UiTheme;
import io.github.forgestove.flexconfig.client.gui.im.model.*;
import io.github.forgestove.flexconfig.client.gui.im.widget.UiButton;
import io.github.forgestove.flexconfig.client.gui.im.widget.UiButton.Style;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import net.minecraft.network.chat.Component;
import java.util.List;
public class KeybindRow extends ValueRow implements CaptureTarget, KeybindConflictAware {
	private final UiButton button;
	private boolean capturing;
	private KeybindState keyState = KeybindState.BOUND;
	private List<Component> conflicts = List.of();
	public KeybindRow(RowContext ctx, ValueConfigNode<?, ?> node) {
		super(ctx, node);
		button = new UiButton(Component.empty(), Style.SECONDARY, this::beginCapture);
		actionBar.add(button, UiTheme.CONTROL_WIDTH);
	}
	private Key key() {
		return (Key) value();
	}
	private void beginCapture() {
		capturing = true;
		ctx.beginCapture(this);
		ctx.refresh();
	}
	@Override
	public boolean captureKey(int keyCode) {
		if (!capturing) return false;
		if (keyCode == InputConstants.KEY_ESCAPE) writeValue(InputConstants.UNKNOWN);
		else writeValue(Type.KEYSYM.getOrCreate(keyCode));
		finish();
		return true;
	}
	@Override
	public void captureMouse(int button) {
		if (!capturing) return;
		writeValue(Type.MOUSE.getOrCreate(button));
		finish();
	}
	private void finish() {
		capturing = false;
		ctx.endCapture(this);
		ctx.refresh();
	}
	@Override
	protected boolean allowLock() {
		return false;
	}
	@Override
	public void refresh() {
		super.refresh();
		button.setEnabled(!state.readOnly);
		var text = key().getDisplayName().copy().withStyle(keyState.color());
		button.setLabel(capturing ? Component.literal("> ").append(text).append(" <") : text);
		if (keyState == KeybindState.CONFLICT && !conflicts.isEmpty()) {
			var used = Component.empty();
			for (var i = 0; i < conflicts.size(); i++) {
				if (i > 0) used = used.append(Component.literal(", "));
				used = used.append(conflicts.get(i));
			}
			button.tooltip(Component.translatable("controls.keybinds.duplicateKeybinds", used));
		} else button.tooltip(null);
	}
	@Override
	public EntryKeybind keybind() {
		return new EntryKeybind(key(), EntryKeybind.NONE);
	}
	@Override
	public Component displayTitle() {
		return node.getTitle();
	}
	@Override
	public void applyConflict(KeybindState state, List<Component> usages) {
		keyState = state;
		conflicts = usages;
	}
}
