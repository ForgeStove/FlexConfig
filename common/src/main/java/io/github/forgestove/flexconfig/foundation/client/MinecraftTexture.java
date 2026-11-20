package io.github.forgestove.flexconfig.foundation.client;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.foundation.ui.core.UiTexture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
/** Minecraft 的贴图实现。按 ResourceLocation 读取原始像素尺寸并缓存，handle() 交给渲染目标用于 blit。 */
public record MinecraftTexture(ResourceLocation id) implements UiTexture {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<ResourceLocation, Optional<int[]>> SIZES = new HashMap<>();
	private static int @Nullable [] size(ResourceLocation id) {
		return SIZES.computeIfAbsent(
			id, key -> {
				var resource = ClientUtil.mc.getResourceManager().getResource(key);
				if (resource.isEmpty()) {
					LOGGER.warn("Missing texture {}", key);
					return Optional.empty();
				}
				try (var image = NativeImage.read(resource.get().open())) {
					return Optional.of(new int[]{image.getWidth(), image.getHeight()});
				} catch (Exception e) {
					LOGGER.warn("Failed to read texture {}", key, e);
					return Optional.empty();
				}
			}
		).orElse(null);
	}
	@Override
	public int width() {
		var size = size(id);
		return size == null ? 0 : size[0];
	}
	@Override
	public int height() {
		var size = size(id);
		return size == null ? 0 : size[1];
	}
	@Override
	public Object handle() {
		return id;
	}
}
