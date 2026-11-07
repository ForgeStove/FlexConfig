package io.github.forgestove.flexconfig.platform.services;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
/** 加载器无关的基础能力；服务端也会加载。 */
public interface PlatformService {
	Path configDir();
	boolean isModLoaded(String modId);
	boolean isClient();
	/** 只用来跑开发期检查，例如缺失翻译键告警。 */
	boolean isDevelopmentEnvironment();
	/** 该模组未加载时返回 {@code null}。 */
	@Nullable String modDisplayName(String modId);
}
