package io.github.forgestove.flexconfig.platform.services;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
/** common 里的代码只通过这个接口访问加载器，Fabric 和 NeoForge 各提供一份实现。 */
public interface PlatformService {
	Path configDir();
	boolean isModLoaded(String modId);
	boolean isClient();
	/** 只用来跑开发期检查，例如缺失翻译键告警。 */
	boolean isDevelopmentEnvironment();
	/** 该模组未加载时返回 {@code null}。 */
	@Nullable String modDisplayName(String modId);
}
