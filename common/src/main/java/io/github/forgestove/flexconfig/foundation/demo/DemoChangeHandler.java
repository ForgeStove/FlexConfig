package io.github.forgestove.flexconfig.foundation.demo;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.api.registration.ConfigChangeHandler;
import org.slf4j.Logger;
/** {@code @OnChange} 示例：保存时收到旧值与新值。 */
public class DemoChangeHandler implements ConfigChangeHandler {
	private static final Logger LOGGER = LogUtils.getLogger();
	@Override
	public void onChange(Object oldValue, Object newValue) {
		LOGGER.info("Demo option changed: {} -> {}", oldValue, newValue);
	}
}
