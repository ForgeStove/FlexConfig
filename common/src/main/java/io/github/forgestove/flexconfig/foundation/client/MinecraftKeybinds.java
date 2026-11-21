package io.github.forgestove.flexconfig.foundation.client;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.*;
import io.github.forgestove.flexconfig.foundation.tree.CategoryConfigNode;
import io.github.forgestove.flexconfig.foundation.ui.core.UiText;
import io.github.forgestove.flexconfig.foundation.ui.factory.*;
import io.github.forgestove.flexconfig.foundation.ui.model.RowTypeRegistry;
import io.github.forgestove.flexconfig.foundation.ui.row.KeybindRow;
import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.*;
/** 按键绑定的 Minecraft 实现：原版 KeyMapping、键盘状态和配置值类型都收在这里。 */
public final class MinecraftKeybinds implements KeybindSupport {
	public static final MinecraftKeybinds INSTANCE = new MinecraftKeybinds();
	static {
		RowTypeRegistry.register(Key.class, KeybindRow::new);
	}
	private static Key key(EntryKeybind keybind) {
		var type = switch (keybind.type()) {
			case KEYSYM -> Type.KEYSYM;
			case SCANCODE -> Type.SCANCODE;
			case MOUSE -> Type.MOUSE;
		};
		return type.getOrCreate(keybind.code());
	}
	@Override
	public UiText displayName(EntryKeybind keybind) {
		var text = UiText.nativeText(key(keybind).getDisplayName());
		if (keybind.modifierFlags() != EntryKeybind.NONE) {
			var prefix = UiText.empty();
			if ((keybind.modifierFlags() & EntryKeybind.CONTROL) != 0) prefix = prefix.append("Ctrl+");
			if ((keybind.modifierFlags() & EntryKeybind.SHIFT) != 0) prefix = prefix.append("Shift+");
			if ((keybind.modifierFlags() & EntryKeybind.ALT) != 0) prefix = prefix.append("Alt+");
			text = prefix.append(text);
		}
		return text.withStyle(keybind.isUnbound() ? KeybindState.UNBOUND.color() : KeybindState.BOUND.color());
	}
	@Override
	public @Nullable UiText displayValue(Object value) {
		if (!(value instanceof Key key)) return null;
		try {
			return UiText.nativeText(key.getDisplayName());
		} catch (RuntimeException ignored) {
			return UiText.translatable(key.getName());
		}
	}
	@Override
	public EntryKeybind fromRaw(Object key) {
		if (!(key instanceof Key raw)) return EntryKeybind.UNBOUND;
		var type = switch (raw.getType()) {
			case KEYSYM -> EntryKeybind.Type.KEYSYM;
			case SCANCODE -> EntryKeybind.Type.SCANCODE;
			case MOUSE -> EntryKeybind.Type.MOUSE;
		};
		return new EntryKeybind(type, raw.getValue(), EntryKeybind.NONE);
	}
	@Override
	public Object rawKey(EntryKeybind keybind) {
		return key(keybind);
	}
	@Override
	public Object unboundRawKey() {
		return InputConstants.UNKNOWN;
	}
	@Override
	public EntryKeybind captureKey(int keyCode) {
		return new EntryKeybind(EntryKeybind.Type.KEYSYM, keyCode, activeModifiers());
	}
	@Override
	public EntryKeybind captureMouse(int button) {
		return new EntryKeybind(EntryKeybind.Type.MOUSE, button, activeModifiers());
	}
	@Override
	public boolean isModifier(int keyCode) {
		return keyCode == InputConstants.KEY_LSHIFT
			|| keyCode == InputConstants.KEY_RSHIFT
			|| keyCode == InputConstants.KEY_LCONTROL
			|| keyCode == InputConstants.KEY_RCONTROL
			|| keyCode == InputConstants.KEY_LALT
			|| keyCode == InputConstants.KEY_RALT;
	}
	/** 未绑定修饰键时要求当前没有修饰键按下，否则 Ctrl+S 也会触发绑定为 S 的动作。 */
	@Override
	public boolean isPressed(long window, EntryKeybind keybind) {
		if (keybind.isUnbound()) return false;
		var raw = key(keybind);
		var keyDown = switch (keybind.type()) {
			case KEYSYM, SCANCODE -> InputConstants.isKeyDown(window, raw.getValue());
			case MOUSE -> GLFW.glfwGetMouseButton(window, raw.getValue()) == GLFW.GLFW_PRESS;
		};
		if (!keyDown) return false;
		if (keybind.modifierFlags() == EntryKeybind.NONE) return !isAnyModifierDown(window);
		if ((keybind.modifierFlags() & EntryKeybind.SHIFT) != 0 && !shiftDown(window)) return false;
		if ((keybind.modifierFlags() & EntryKeybind.CONTROL) != 0 && !controlDown(window)) return false;
		return (keybind.modifierFlags() & EntryKeybind.ALT) == 0 || altDown(window);
	}
	@Override
	public List<UiText> registeredUsages(EntryKeybind keybind) {
		var physical = keybind.physicalKey();
		var usages = new ArrayList<UiText>();
		for (var mapping : ClientUtil.mc.options.keyMappings) {
			var raw = ClientUtil.boundKey(mapping);
			if (raw.equals(InputConstants.UNKNOWN)) continue;
			if (!fromRaw(raw).physicalKey().equals(physical)) continue;
			usages.add(UiText.translatable(mapping.getName()));
		}
		return usages;
	}
	@Override
	public @Nullable CategoryConfigNode buildCategory(String modId) {
		var modMappings = new ArrayList<KeyMapping>();
		for (var mapping : ClientUtil.mc.options.keyMappings)
			if (mapping.getCategory().equals("key.categories." + modId)) modMappings.add(mapping);
		if (modMappings.isEmpty()) return null;
		var builder = CategoryConfigNode.builder().title(Translation.KEYBINDS_LABEL);
		modMappings.forEach(mapping -> builder.value(value -> value.valueType(Key.class)
			.name(mapping.getName())
			.path("keybinds." + mapping.getName())
			.title(UiText.translatable(mapping.getName()))
			.defaultValue(mapping.getDefaultKey())
			.valueReader(config -> ClientUtil.boundKey(mapping))
			.valueWriter((config, valueKey) -> mapping.setKey((Key) valueKey))
			.restartType(null)));
		return builder.build();
	}
	@Override
	public void saveOptions() {
		ClientUtil.mc.options.save();
	}
	@Override
	public boolean singleplayerServer() {
		return ClientUtil.mc.hasSingleplayerServer();
	}
	private static int activeModifiers() {
		var window = ClientUtil.mc.getWindow().getWindow();
		var flags = EntryKeybind.NONE;
		if (shiftDown(window)) flags |= EntryKeybind.SHIFT;
		if (controlDown(window)) flags |= EntryKeybind.CONTROL;
		if (altDown(window)) flags |= EntryKeybind.ALT;
		return flags;
	}
	private static boolean shiftDown(long window) {
		return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
	}
	private static boolean controlDown(long window) {
		return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL) || InputConstants.isKeyDown(
			window,
			GLFW.GLFW_KEY_RIGHT_CONTROL
		);
	}
	private static boolean altDown(long window) {
		return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_ALT) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_ALT);
	}
	private static boolean isAnyModifierDown(long window) {
		return shiftDown(window) || controlDown(window) || altDown(window);
	}
}
