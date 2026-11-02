package io.github.forgestove.flexconfig.demo;
import io.github.forgestove.flexconfig.ConfigRegistry;
public final class DemoRegistration {
	public static void register() {
		ConfigRegistry.init(DemoConfig.class);
		ConfigRegistry.init(DemoServerConfig.class);
	}
}
