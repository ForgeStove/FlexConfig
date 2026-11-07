package io.github.forgestove.flexconfig.foundation.tree;
import com.google.common.collect.ImmutableList;
import io.github.forgestove.flexconfig.foundation.ConfigChangeDispatcher;
import io.github.forgestove.flexconfig.api.RestartType;
import io.github.forgestove.flexconfig.foundation.client.Translation;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;

import java.util.function.UnaryOperator;
public class CategoryConfigNode implements ConfigNode {
	private Component title;
	private Component tooltip;
	private ImmutableList<ConfigNode> children;
	public static Builder builder() {
		return new Builder();
	}
	@NotNull
	public Component getTitle() {
		return title;
	}
	@Nullable
	@Override
	public Component getTooltip() {
		return tooltip;
	}
	@Override
	public void resetToDefault() {
		children.forEach(ConfigNode::resetToDefault);
	}
	@Override
	public void resetToActive(Object config) {
		children.forEach(child -> child.resetToActive(config));
	}
	@Nullable
	@Override
	public RestartType restartType(Object config) {
		RestartType worst = null;
		for (var node : children) {
			var type = node.restartType(config);
			if (type == RestartType.GAME) return RestartType.GAME;
			if (type == RestartType.WORLD) worst = RestartType.WORLD;
		}
		return worst;
	}
	@Override
	public boolean isDefaultValue(Object config) {
		for (var node : children) if (!node.isDefaultValue(config)) return false;
		return true;
	}
	@Override
	public boolean isActiveValue(Object config) {
		for (var node : children) if (!node.isActiveValue(config)) return false;
		return true;
	}
	@Nullable
	@Override
	public Component validate(Object config) {
		Component error = null;
		for (var node : children) {
			var result = node.validate(config);
			if (result == null) continue;
			if (error == null) error = result;
			else return Translation.MULTIPLE_ERRORS;
		}
		return error;
	}
	@Override
	public void copy(Object from, Object to) {
		children.forEach(node -> node.copy(from, to));
	}
	@Override
	public void copyForRuntime(Object from, Object to) {
		children.forEach(node -> node.copyForRuntime(from, to));
	}
	@Override
	public void flushPending(Object to) {
		children.forEach(node -> node.flushPending(to));
	}
	@Override
	public void applyRestart(Object saved, Object active, RestartType restartType) {
		ConfigChangeDispatcher.begin();
		try {
			children.forEach(node -> node.applyRestart(saved, active, restartType));
		} finally {
			ConfigChangeDispatcher.end();   // finally：节点抛异常也不会让派发深度卡住
		}
	}
	@Override
	public void writeEditingToConfig(Object config) {
		ConfigChangeDispatcher.begin();
		try {
			children.forEach(node -> node.writeEditingToConfig(config));
		} finally {
			ConfigChangeDispatcher.end();   // finally：节点抛异常也不会让派发深度卡住
		}
	}
	@NotNull
	public ImmutableList<ConfigNode> getChildren() {
		return children;
	}
	@SuppressWarnings("UnusedReturnValue")
	public static class Builder {
		private CategoryConfigNode node;
		private ImmutableList.Builder<ConfigNode> childrenBuilder;
		private Builder() {
			node = new CategoryConfigNode();
			childrenBuilder = ImmutableList.builder();
		}
		public Builder title(Component title) {
			node.title = title;
			return this;
		}
		public Builder tooltip(Component tooltip) {
			node.tooltip = tooltip;
			return this;
		}
		public Builder value(UnaryOperator<ValueConfigNode.Builder> valueBuilder) {
			childrenBuilder.add(valueBuilder.apply(ValueConfigNode.builder()).build());
			return this;
		}
		public Builder category(UnaryOperator<Builder> categoryBuilder) {
			childrenBuilder.add(categoryBuilder.apply(new Builder()).build());
			return this;
		}
		public CategoryConfigNode build() {
			var n = node;
			n.children = childrenBuilder.build();
			node = null;
			childrenBuilder = null;
			return n;
		}
	}
}
