package io.github.forgestove.flexconfig.client.gui.entry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
/** 数组字段/数组元素的 List 视图：任何增删改都立刻写回目标数组。 */
final class WriteBackList extends ArrayList<Object> {
	private final Runnable onChange;
	private boolean updating;
	WriteBackList(List<?> values, Runnable onChange) {
		super(values);
		this.onChange = onChange;
	}
	private void changed() {
		if (!updating) onChange.run();
	}
	@Override
	public Object set(int index, Object element) {
		var previous = super.set(index, element);
		changed();
		return previous;
	}
	@Override
	public boolean add(Object element) {
		var added = super.add(element);
		changed();
		return added;
	}
	@Override
	public void add(int index, Object element) {
		super.add(index, element);
		changed();
	}
	@Override
	public Object remove(int index) {
		var removed = super.remove(index);
		changed();
		return removed;
	}
	@Override
	public boolean remove(Object element) {
		var removed = super.remove(element);
		changed();
		return removed;
	}
	@Override
	public void clear() {
		super.clear();
		changed();
	}
	@Override
	public boolean addAll(Collection<?> collection) {
		var added = super.addAll(collection);
		changed();
		return added;
	}
	@Override
	public boolean addAll(int index, Collection<?> collection) {
		var added = super.addAll(index, collection);
		changed();
		return added;
	}
	@Override
	public boolean removeAll(Collection<?> collection) {
		var removed = super.removeAll(collection);
		changed();
		return removed;
	}
	@Override
	public boolean retainAll(Collection<?> collection) {
		var retained = super.retainAll(collection);
		changed();
		return retained;
	}
	/** 用新内容替换；传入自身时内容已就地变更，不再重复回写。 */
	void replaceContents(List<?> values) {
		if (values == this) return;
		updating = true;
		try {
			super.clear();
			super.addAll(values);
		} finally {
			updating = false;
		}
		changed();
	}
}
