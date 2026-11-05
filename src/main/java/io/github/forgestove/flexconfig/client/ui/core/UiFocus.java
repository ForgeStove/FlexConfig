package io.github.forgestove.flexconfig.client.ui.core;
import java.util.*;
/** 焦点顺序：每帧由界面重建，结构变化后当前焦点若已不存在会自动清除。 */
public class UiFocus {
	public interface Target {
		@SuppressWarnings("unused")
		UiBounds focusBounds();
	}
	private List<Target> order = new ArrayList<>();
	private Target current;
	public void setOrder(List<? extends Target> targets) {
		order = new ArrayList<>(targets);
		if (current != null && !containsIdentity(current)) current = null;
	}
	public Target current() {
		return current;
	}
	public boolean is(Target target) {
		return current == target;
	}
	public void set(Target target) {
		current = target;
	}
	public void clear() {
		current = null;
	}
	public boolean containsIdentity(Target target) {
		for (var candidate : order) if (candidate == target) return true;
		return false;
	}
	/** 在焦点顺序里移动一格并回绕；没有可聚焦项时返回 false。 */
	public void move(int direction) {
		if (order.isEmpty()) return;
		var size = order.size();
		var index = indexOf(current);
		index = index < 0 ? direction > 0 ? 0 : size - 1 : Math.floorMod(index + direction, size);
		current = order.get(index);
	}
	private int indexOf(Target target) {
		if (target == null) return -1;
		for (var i = 0; i < order.size(); i++) if (order.get(i) == target) return i;
		return -1;
	}
}
