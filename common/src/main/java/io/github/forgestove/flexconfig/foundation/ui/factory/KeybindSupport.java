package io.github.forgestove.flexconfig.foundation.ui.factory;
import io.github.forgestove.flexconfig.foundation.tree.CategoryConfigNode;
import io.github.forgestove.flexconfig.foundation.ui.core.UiText;
import org.jetbrains.annotations.Nullable;

import java.util.List;
public interface KeybindSupport {
	UiText displayName(EntryKeybind keybind);
	/** 配置值是指针类型（原版按键）时返回显示文本，否则返回 null。 */
	@Nullable UiText displayValue(Object value);
	EntryKeybind fromRaw(Object key);
	/** 把平台按键转成配置值类型，供保存原版按键映射的字段使用。 */
	Object rawKey(EntryKeybind keybind);
	Object unboundRawKey();
	EntryKeybind captureKey(int keyCode);
	EntryKeybind captureMouse(int button);
	boolean isModifier(int keyCode);
	boolean isPressed(long window, EntryKeybind keybind);
	List<UiText> registeredUsages(EntryKeybind keybind);
	@Nullable CategoryConfigNode buildCategory(String modId);
	void saveOptions();
	boolean singleplayerServer();
}
