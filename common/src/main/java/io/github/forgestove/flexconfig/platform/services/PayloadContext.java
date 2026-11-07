package io.github.forgestove.flexconfig.platform.services;
import org.jetbrains.annotations.Nullable;
import net.minecraft.server.level.ServerPlayer;
/** 包处理器上下文的最小面。 */
public interface PayloadContext {
	@Nullable ServerPlayer player();
	void enqueue(Runnable task);
}
