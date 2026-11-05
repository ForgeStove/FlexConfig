package io.github.forgestove.flexconfig.demo;
import io.github.forgestove.flexconfig.ConfigHandler;
public class DemoRegistration {
	public static void register() {
		ConfigHandler.builder(DemoConfig.class).build();
		ConfigHandler.builder(DemoServerConfig.class).build();
	}
}
