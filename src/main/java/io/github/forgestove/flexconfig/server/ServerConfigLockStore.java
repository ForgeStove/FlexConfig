package io.github.forgestove.flexconfig.server;
import com.electronwill.nightconfig.core.*;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.ConfigSerializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.nio.file.*;
import java.util.*;
/**
 * 按世界存档存储锁定的配置条目，落盘为存档下的 {@code serverconfig/flexconfig/<modId>_locks.toml}（结构与配置文件一致）。
 * <p>
 * 放在 {@code flexconfig} 子目录是为了不和官方认的「server 配置覆盖文件」混在一起。
 */
public final class ServerConfigLockStore {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Path lockFilePath;
	/** 锁定项，结构与配置文件一致；键由客户端传来的路径按点号分层。 */
	private CommentedConfig locks = ConfigSerializer.inMemory();
	public ServerConfigLockStore(MinecraftServer server, String modId) {
		lockFilePath = server.getWorldPath(LevelResource.ROOT).resolve("serverconfig/flexconfig/" + modId + "_locks.toml");
	}
	private static void collectPaths(UnmodifiableConfig config, String prefix, Set<String> out) {
		for (var entry : config.entrySet()) {
			var path = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
			var value = entry.getValue();
			if (value instanceof UnmodifiableConfig sub) collectPaths(sub, path, out);
			else out.add(path);
		}
	}
	/** 服务器启动时调用。 */
	public void load() {
		var path = lockFilePath;
		if (!Files.exists(path)) {
			locks = ConfigSerializer.inMemory();
			return;
		}
		try {
			var parsed = ConfigSerializer.read(path);
			locks = parsed == null ? ConfigSerializer.inMemory() : parsed;
			LOGGER.info("Loaded {} config lock(s)", lockedPaths().size());
		} catch (Exception e) {
			LOGGER.error("Failed to load config locks from {}", path, e);
			locks = ConfigSerializer.inMemory();
		}
	}
	/** 将配置条目锁定为特定的值（已由 {@link io.github.forgestove.flexconfig.LockValueCodec} 转成 TOML 原生类型）。 */
	public void lockEntry(String configId, Object value) {
		locks.set(splitPath(configId), value);
		save();
	}
	private static List<String> splitPath(String configId) {
		return List.of(configId.split("\\."));
	}
	/** 服务器停止时与每次更改后调用。 */
	public void save() {
		try {
			Files.createDirectories(lockFilePath.getParent());
			new TomlWriter().write(locks, lockFilePath, WritingMode.REPLACE_ATOMIC);
		} catch (Exception e) {
			LOGGER.error("Failed to save config locks to {}", lockFilePath, e);
		}
	}
	/** 从锁定存储中移除。 */
	public void unlockEntry(String configId) {
		locks.remove(splitPath(configId));
		save();
	}
	/** 当前锁定的配置项路径（点分形式）的只读视图。 */
	public Set<String> lockedPaths() {
		var result = new LinkedHashSet<String>();
		collectPaths(locks, "", result);
		return Collections.unmodifiableSet(result);
	}
	/** 锁定项的结构化表示，供服务端直接应用（无需再走文本往返）。 */
	public CommentedConfig toConfig() {
		return locks;
	}
	/** 锁定项的 TOML 文本，供下发给客户端。 */
	public String toTomlString() {
		return new TomlWriter().writeToString(locks);
	}
}
