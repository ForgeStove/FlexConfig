package io.github.forgestove.flexconfig.foundation.ui.model;
import io.github.forgestove.flexconfig.foundation.ui.row.ConfigRow;
import org.jetbrains.annotations.Nullable;

import java.util.*;
public class RemovalQueue {
	private record Pending(List<ConfigRow> rows, Runnable action, @Nullable String expandPath) {
		boolean finished() {
			for (var row : rows) if (!row.exited()) return false;
			return true;
		}
	}
	private final List<Pending> pending = new ArrayList<>();
	public boolean isEmpty() {
		return pending.isEmpty();
	}
	public boolean finished() {
		for (var item : pending) if (!item.finished()) return false;
		return true;
	}
	/** @return 没有匹配行时返回 true，调用方应立即执行动作并重建列表。 */
	public boolean exitRow(Object identity, List<ConfigRow> rows, Runnable action) {
		var key = String.valueOf(identity);
		var matched = new ArrayList<ConfigRow>();
		for (var row : rows) {
			var id = row.identity();
			if (id == null) continue;
			var path = id.toString();
			// 标题行连同它的字段行、嵌套元素行作为一整棵子树一起收起
			if (path.equals(key) || path.startsWith(key + ".") || path.startsWith(key + "[")) matched.add(row);
		}
		if (matched.isEmpty()) {
			action.run();
			return true;
		}
		if (matched.stream().allMatch(ConfigRow::exiting)) return false;   // 重复点击，已经在收起
		for (var row : matched) row.onExit();
		pending.add(new Pending(matched, action, null));
		return false;
	}
	/** 收起一个集合的全部元素行，重建后让新的元素行重新展开；用于集合重置。 */
	public void exitElements(String collectionPath, List<ConfigRow> rows, Runnable action) {
		var prefix = collectionPath + "[";
		var matched = new ArrayList<ConfigRow>();
		for (var row : rows) {
			var id = row.identity();
			if (id != null && id.toString().startsWith(prefix)) matched.add(row);
		}
		if (!matched.isEmpty() && matched.stream().allMatch(ConfigRow::exiting)) return;   // 重复点击，已经在收起
		for (var row : matched) row.onExit();
		pending.add(new Pending(matched, action, collectionPath));
	}
	/** 立即结束所有待处理的删除；返回重建后需要重新展开的集合路径。 */
	public List<String> flush() {
		if (pending.isEmpty()) return List.of();
		var items = List.copyOf(pending);
		pending.clear();
		var expansions = new ArrayList<String>();
		for (var item : items) {
			item.action().run();
			if (item.expandPath() != null) expansions.add(item.expandPath());
		}
		return expansions;
	}
}
