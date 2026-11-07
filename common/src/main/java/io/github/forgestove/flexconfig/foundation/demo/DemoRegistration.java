package io.github.forgestove.flexconfig.foundation.demo;
import io.github.forgestove.flexconfig.api.registration.ConfigHandler;
public class DemoRegistration {
	public static void register() {
		ConfigHandler.builder(DemoConfig.class).build();
		ConfigHandler.builder(DemoServerConfig.class).build();
	}
}
