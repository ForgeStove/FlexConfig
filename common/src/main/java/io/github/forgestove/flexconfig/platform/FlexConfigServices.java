package io.github.forgestove.flexconfig.platform;
import io.github.forgestove.flexconfig.platform.services.NetworkService;
import io.github.forgestove.flexconfig.platform.services.PlatformService;

import java.util.ServiceLoader;
/** 服务端也会加载的平台服务入口。客户端专属服务见 {@link FlexConfigClientServices}。 */
public class FlexConfigServices {
	public static final PlatformService PLATFORM = load(PlatformService.class);
	public static final NetworkService NETWORK = load(NetworkService.class);
	protected static <T> T load(Class<T> clazz) {
		return ServiceLoader.load(clazz, FlexConfigServices.class.getClassLoader())
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("Failed to load service " + clazz.getName()));
	}
}
