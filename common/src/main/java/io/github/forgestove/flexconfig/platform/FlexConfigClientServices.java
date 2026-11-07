package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.platform.services.*;
public class FlexConfigClientServices extends FlexConfigServices {
	public static final ClientUiService CLIENT_UI = load(ClientUiService.class);
	public static final KeybindService KEYBINDS = load(KeybindService.class);
}
