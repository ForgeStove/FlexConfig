package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.platform.services.*;

import java.util.ServiceLoader;
public class FlexConfigServices {
	public static final PlatformService PLATFORM = load(PlatformService.class);
	public static final NetworkService NETWORK = load(NetworkService.class);
	protected static <T> T load(Class<T> clazz) {
		return ServiceLoader.load(clazz, FlexConfigServices.class.getClassLoader())
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("Failed to load service " + clazz.getName()));
	}
}
