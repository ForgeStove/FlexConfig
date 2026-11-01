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
/** 按世界存档存储锁定的配置条目，落盘为存档下的 {@code serverconfig/flexconfig/<modId>_locks.toml}；{@code flexconfig} 子目录是为了不和官方认的「server 配置覆盖文件」混在一起。 */
public final class ServerConfigLockStore {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Path lockFilePath;
	/** 锁定项结构同配置文件；键由客户端传来的路径按点号分层。 */
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
	private static List<String> splitPath(String configId) {
		return List.of(configId.split("\\."));
	}
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
	/** {@code value} 已由 {@link io.github.forgestove.flexconfig.LockValueCodec} 转为 TOML 原生类型。 */
	public void lockEntry(String configId, Object value) {
		locks.set(splitPath(configId), value);
		save();
	}
	public void save() {
		try {
			Files.createDirectories(lockFilePath.getParent());
			new TomlWriter().write(locks, lockFilePath, WritingMode.REPLACE_ATOMIC);
		} catch (Exception e) {
			LOGGER.error("Failed to save config locks to {}", lockFilePath, e);
		}
	}
	public void unlockEntry(String configId) {
		locks.remove(splitPath(configId));
		save();
	}
	public Set<String> lockedPaths() {
		var result = new LinkedHashSet<String>();
		collectPaths(locks, "", result);
		return Collections.unmodifiableSet(result);
	}
	/** 直接交出底层配置对象，服务端应用时无需再走文本往返。 */
	public CommentedConfig toConfig() {
		return locks;
	}
	/** 锁定项的 TOML 文本，供下发给客户端。 */
	public String toTomlString() {
		return new TomlWriter().writeToString(locks);
	}
}
