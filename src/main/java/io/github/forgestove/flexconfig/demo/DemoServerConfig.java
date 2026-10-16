package io.github.forgestove.flexconfig.demo;
import io.github.forgestove.flexconfig.FlexConfig;
import io.github.forgestove.flexconfig.api.*;

import java.util.*;
/** 开发期演示配置的服务端一份：进世界才知道存档目录，故 {@link ConfigType#SERVER} 在连接时下发给客户端且客户端只读；与 {@link DemoConfig} 共用模组 ID，因此模组列表的入口会先进配置文件选择界面。 */
@Config(value = FlexConfig.ID, type = ConfigType.SERVER)
@SuppressWarnings("unused") // 配置字段全部由反射读取
public final class DemoServerConfig {
	@Category public Gameplay gameplay = new Gameplay();
	@Category public Limits limits = new Limits();
	public static class Gameplay {
		@IntRange(min = 1, max = 64, slider = true) public int maxPlayersPerTeam = 8;
		public boolean friendlyFire = true;
		public boolean keepInventory = false;
		@RequiresRestart(RestartType.WORLD) public boolean hardcore = false;
		public List<String> whitelist = new ArrayList<>(List.of("Steve", "Alex"));
		@StringLength(min = 1, max = 32) public String motd = "Flex Config demo";
		public boolean announceDeaths = true;
	}
	public static class Limits {
		@IntRange(min = 1, max = 1000) public int maxEntitiesPerChunk = 128;
		@LongRange(min = 0) public long worldSeed = 0L;
		@DoubleRange(min = 0.0, max = 100.0, slider = true) public double mobSpawnRate = 1.0;
		@WarnCheat public boolean allowCheats = false;
		@Condition(value = "minecraft") public boolean visibleWithMod = true;
	}
}
