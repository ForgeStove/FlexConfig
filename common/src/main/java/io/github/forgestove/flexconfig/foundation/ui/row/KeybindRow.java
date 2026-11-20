package io.github.forgestove.flexconfig.foundation.ui.row;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.*;
import io.github.forgestove.flexconfig.foundation.client.Translation;
import io.github.forgestove.flexconfig.foundation.tree.ValueConfigNode;
import io.github.forgestove.flexconfig.foundation.ui.core.*;
import io.github.forgestove.flexconfig.foundation.ui.factory.*;
import io.github.forgestove.flexconfig.foundation.ui.model.*;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiButton;
import io.github.forgestove.flexconfig.foundation.ui.widget.UiButton.Style;

import java.util.List;
public class KeybindRow extends ValueRow implements CaptureTarget, KeybindConflictAware {
	private final UiButton button;
	private boolean capturing;
	private KeybindState keyState = KeybindState.BOUND;
	private List<UiText> conflicts = List.of();
	public KeybindRow(RowContext ctx, ValueConfigNode node) {
		super(ctx, node);
		button = new UiButton(UiText.empty(), Style.KEYBIND, this::beginCapture);
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
	/** 按键名的翻译由原版懒加载缓存提供，损坏时退回原始键名，避免一行拖垮整个界面构建。 */
	private UiText displayName() {
		var key = key();
		try {
			return UiText.from(key.getDisplayName());
		} catch (RuntimeException ignored) {
			return UiText.translatable(key.getName());
		}
	}
	@Override
	public void refresh() {
		super.refresh();
		button.setEnabled(!state.readOnly);
		button.setLabel(displayName());
		button.setActive(capturing);
		if (keyState == KeybindState.CONFLICT && !conflicts.isEmpty()) {
			var used = UiText.empty();
			for (var i = 0; i < conflicts.size(); i++) {
				if (i > 0) used = used.append(", ");
				used = used.append(conflicts.get(i));
			}
			button.tooltip(UiText.translatable("controls.keybinds.duplicateKeybinds", used));
		} else button.tooltip(null);
	}
	@Override
	public EntryKeybind keybind() {
		return new EntryKeybind(key(), EntryKeybind.NONE);
	}
	@Override
	public UiText displayTitle() {
		return Translation.from(node.getTitle());
	}
	@Override
	public void applyConflict(KeybindState state, List<UiText> usages) {
		keyState = state;
		conflicts = usages;
	}
}
