package io.github.forgestove.flexconfig;
import io.github.forgestove.flexconfig.demo.DemoRegistration;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
@Mod(FlexConfig.ID)
public class ForgeEntry {
	public ForgeEntry() {
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> DemoRegistration::register);
	}
}
