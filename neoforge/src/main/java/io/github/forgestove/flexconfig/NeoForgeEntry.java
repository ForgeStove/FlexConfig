package io.github.forgestove.flexconfig;
import io.github.forgestove.flexconfig.foundation.demo.DemoRegistration;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
@Mod(FlexConfig.ID)
public class NeoForgeEntry {
	public NeoForgeEntry() {
		if (FMLEnvironment.dist.isClient()) DemoRegistration.register();
	}
}
