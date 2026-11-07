package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.platform.services.ClientUiService;
import io.github.forgestove.flexconfig.platform.services.KeybindService;
/** 客户端专属服务入口；共享代码必须先确认 {@link FlexConfigServices#PLATFORM} 为客户端再触碰。 */
public class FlexConfigClientServices extends FlexConfigServices {
	public static final ClientUiService CLIENT_UI = load(ClientUiService.class);
	public static final KeybindService KEYBINDS = load(KeybindService.class);
}
