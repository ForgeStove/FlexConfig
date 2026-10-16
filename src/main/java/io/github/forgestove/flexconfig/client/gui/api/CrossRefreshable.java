package io.github.forgestove.flexconfig.client.gui.api;
import io.github.forgestove.flexconfig.client.gui.entry.ConfigEntry;

import java.util.List;
/**
 * 需要同级别其他 entry 状态才能刷新的入口实现此接口：列表刷新时按序调用各 entry，遇到返回 true 的即停，
 * 键位冲突检测依赖这一顺序。
 */
public interface CrossRefreshable {
	boolean beginCrossEntryRefresh(List<ConfigEntry> siblings);
}
