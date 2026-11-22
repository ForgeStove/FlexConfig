package io.github.forgestove.flexconfig.foundation.ui.factory;
import io.github.forgestove.flexconfig.foundation.ui.core.UiText;
public record EntryKeybind(Type type, int code, int modifierFlags) {
	public enum Type {
		KEYSYM,
		SCANCODE,
		MOUSE
	}
	public static final int NONE = 0;
	public static final int SHIFT = 1;
	public static final int CONTROL = 1 << 1;
	public static final int ALT = 1 << 2;
	public static final EntryKeybind UNBOUND = new EntryKeybind(Type.KEYSYM, -1, NONE);
	public static EntryKeybind deserialize(String str) {
		if (str == null || str.isEmpty()) return UNBOUND;
		var parts = str.split("\\+");
		var codeStr = parts[parts.length - 1];
		var flags = NONE;
		for (var i = 0; i < parts.length - 1; i++) {
			var m = parts[i].toLowerCase();
			flags |= switch (m) {
				case "shift", "s" -> SHIFT;
				case "control", "ctrl", "c" -> CONTROL;
				case "alt", "a" -> ALT;
				default -> NONE;
			};
		}
		try {
			var type = Type.KEYSYM;
			if (codeStr.startsWith("mouse:")) {
				type = Type.MOUSE;
				codeStr = codeStr.substring(6);
			} else if (codeStr.startsWith("scan:")) {
				type = Type.SCANCODE;
				codeStr = codeStr.substring(5);
			}
			return new EntryKeybind(type, Integer.parseInt(codeStr), flags);
		} catch (NumberFormatException e) {
			return UNBOUND;
		}
	}
	public boolean isUnbound() {
		return type == Type.KEYSYM && code == -1;
	}
	/** 冲突判断只看物理按键，不区分修饰键。 */
	public EntryKeybind physicalKey() {
		return modifierFlags == NONE ? this : new EntryKeybind(type, code, NONE);
	}
	public UiText getDisplayName() {
		return Keybinds.support().displayName(this);
	}
	/**
	 * 序列化为 {@code "[modifiers+]code"}，修饰键记单字母 {@code c}/{@code s}/{@code a}，如 {@code "c+s+83"}；鼠标与扫描码分别带 {@code mouse:}、{@code scan:}
	 * 前缀。
	 */
	public String serialize() {
		if (isUnbound()) return "";
		var sb = new StringBuilder();
		if ((modifierFlags & CONTROL) != 0) sb.append("c+");
		if ((modifierFlags & SHIFT) != 0) sb.append("s+");
		if ((modifierFlags & ALT) != 0) sb.append("a+");
		var typePrefix = switch (type) {
			case MOUSE -> "mouse:";
			case SCANCODE -> "scan:";
			default -> "";
		};
		sb.append(typePrefix).append(code);
		return sb.toString();
	}
}
