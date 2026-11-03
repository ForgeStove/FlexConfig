package io.github.forgestove.flexconfig;
import io.github.forgestove.flexconfig.demo.DemoRegistration;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
/**
 * NeoForge 的模组入口。本类只负责声明 modId，事件订阅见 {@link ConfigEvents}。
 * <p>
 * Fabric 没有 `@Mod` 概念（入口写在 {@code fabric.mod.json} 的 entrypoint 里），所以这个类不能放在共享源集。
 */
@Mod(FlexConfig.ID)
public class NeoForgeEntry {
	public NeoForgeEntry() {
		if (FMLEnvironment.dist.isClient()) DemoRegistration.register();
	}
}
