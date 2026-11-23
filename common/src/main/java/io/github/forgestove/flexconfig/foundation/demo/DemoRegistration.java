package io.github.forgestove.flexconfig.foundation.demo;
import io.github.forgestove.flexconfig.api.registration.ConfigHandler;
@SuppressWarnings("unused")
public class DemoRegistration {
	public static final DemoConfig config = ConfigHandler.builder(DemoConfig.class).build().getConfig();
	public static final DemoServerConfig serverConfig = ConfigHandler.builder(DemoServerConfig.class).build().getConfig();
	public static void register() {}
}
