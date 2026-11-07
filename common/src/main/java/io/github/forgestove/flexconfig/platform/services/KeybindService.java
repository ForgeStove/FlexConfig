package io.github.forgestove.flexconfig.platform.services;
import com.mojang.blaze3d.platform.InputConstants.Key;
import net.minecraft.client.KeyMapping;
/** 读取快捷键绑定的加载器差异；只在客户端加载。 */
public interface KeybindService {
	Key boundKey(KeyMapping mapping);
}
