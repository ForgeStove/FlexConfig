package io.github.forgestove.flexconfig.foundation.tree;
import io.github.forgestove.flexconfig.api.RestartType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
/** 节点的运行期视图：配置实例与值都按 Object 存取，构建期的具体类型不进入这里的签名。 */
public interface ConfigNode {
	@Nullable Component getTooltip();
	void resetToDefault();
	void resetToActive(Object config);
	@Nullable RestartType restartType(Object config);
	boolean isDefaultValue(Object config);
	boolean isActiveValue(Object config);
	@Nullable Component validate(Object config);
	void copy(Object from, Object to);
	/** 把 from 复制到运行时对象 to，跳过「已改动但尚未生效」的延迟项，它们的运行时值要等重启才更新。 */
	void copyForRuntime(Object from, Object to);
	/** 把「已改动但尚未生效」的新值写入 to（文件模型）；没有待生效改动时是空操作。 */
	void flushPending(Object to);
	/** 让「重启级别等于 {@code restartType}」的延迟项生效：把文件里的新值写入运行时对象 active。 */
	void applyRestart(Object saved, Object active, RestartType restartType);
	void writeEditingToConfig(Object config);
	default String getPath() {
		return "";
	}
}
