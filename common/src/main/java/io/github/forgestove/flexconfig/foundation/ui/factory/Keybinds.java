package io.github.forgestove.flexconfig.foundation.ui.factory;
import io.github.forgestove.flexconfig.foundation.tree.CategoryConfigNode;
import io.github.forgestove.flexconfig.foundation.ui.core.UiText;
import org.jetbrains.annotations.Nullable;

import java.util.List;
/** {@link KeybindSupport} 的静态安装点；实现安装前使用不依赖原版类型的兜底。 */
public final class Keybinds {
	private static KeybindSupport support = Fallback.INSTANCE;
	private Keybinds() {}
	public static void install(KeybindSupport support) {
		if (support != null) Keybinds.support = support;
	}
	public static KeybindSupport support() {
		return support;
	}
	private enum Fallback implements KeybindSupport {
		INSTANCE;
		@Override
		public UiText displayName(EntryKeybind keybind) {
			return UiText.literal(keybind.serialize());
		}
		@Override
		public @Nullable UiText displayValue(Object value) {
			return null;
		}
		@Override
		public EntryKeybind fromRaw(Object key) {
			return EntryKeybind.UNBOUND;
		}
		@Override
		public Object rawKey(EntryKeybind keybind) {
			return keybind.code();
		}
		@Override
		public Object unboundRawKey() {
			return -1;
		}
		@Override
		public EntryKeybind captureKey(int keyCode) {
			return new EntryKeybind(EntryKeybind.Type.KEYSYM, keyCode, EntryKeybind.NONE);
		}
		@Override
		public EntryKeybind captureMouse(int button) {
			return new EntryKeybind(EntryKeybind.Type.MOUSE, button, EntryKeybind.NONE);
		}
		@Override
		public boolean isModifier(int keyCode) {
			return false;
		}
		@Override
		public boolean isPressed(long window, EntryKeybind keybind) {
			return false;
		}
		@Override
		public List<UiText> registeredUsages(EntryKeybind keybind) {
			return List.of();
		}
		@Override
		public @Nullable CategoryConfigNode buildCategory(String modId) {
			return null;
		}
		@Override
		public void saveOptions() {}
		@Override
		public boolean singleplayerServer() {
			return false;
		}
	}
}
