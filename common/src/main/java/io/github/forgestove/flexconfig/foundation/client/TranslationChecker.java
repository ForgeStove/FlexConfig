package io.github.forgestove.flexconfig.foundation.client;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.foundation.tree.RootConfigNode;
import io.github.forgestove.flexconfig.platform.FlexConfigServices;
import net.minecraft.client.resources.language.I18n;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
public class TranslationChecker {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<String, Supplier<Collection<String>>> SOURCES = new ConcurrentHashMap<>();
	/** 注册外部翻译键来源；同一名称重复注册会覆盖。需在客户端首次扫描语言前完成注册。 */
	public static void registerSource(String name, Supplier<Collection<String>> keys) {
		SOURCES.put(name, keys);
	}
	public static void unregisterSource(String name) {
		SOURCES.remove(name);
	}
	/** 缺失的键按行打印，便于直接照抄进语言文件。 */
	public static void check(RootConfigNode root) {
		report("Config '" + root.fileName() + "'", root.translationKeys());
	}
	public static void check(String sourceName, Collection<String> keys) {
		report("Translation source '" + sourceName + "'", keys);
	}
	static void checkExternalSources() {
		SOURCES.forEach((name, source) -> {
			try {
				var keys = source.get();
				if (keys != null) report("Translation source '" + name + "'", keys);
			} catch (Exception e) {
				LOGGER.warn("Translation source '{}' failed to provide keys", name, e);
			}
		});
	}
	private static void report(String source, Collection<String> keys) {
		if (!FlexConfigServices.PLATFORM.isDevelopmentEnvironment()) return;
		if (keys == null || keys.isEmpty()) return;
		var unique = new LinkedHashSet<String>();
		for (var key : keys) if (key != null && !key.isBlank()) unique.add(key);
		var missing = unique.stream().filter(key -> !I18n.exists(key)).toList();
		if (missing.isEmpty()) return;
		var language = ClientUtil.mc.getLanguageManager().getSelected();
		LOGGER.warn(
			"{} is missing {} translation key(s) for language '{}'; the UI will show the raw keys:", source,
			missing.size(),
			language
		);
		missing.forEach(key -> LOGGER.warn("  {}", key));
	}
}
