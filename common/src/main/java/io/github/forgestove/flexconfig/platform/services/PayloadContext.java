package io.github.forgestove.flexconfig.platform.services;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
public interface PayloadContext {
	@Nullable ServerPlayer player();
	void enqueue(Runnable task);
}
