package io.github.forgestove.flexconfig.client.gui.entry;

import io.github.forgestove.flexconfig.client.gui.ConfigCategoryTab;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
/** 单字符输入；空串视为 {@code '\0'}。 */
public class CharacterValueConfigEntry<C> extends GenericValueConfigEntry<C, Character> {
	public CharacterValueConfigEntry(ConfigCategoryTab<C, Character> tab, ValueConfigNode<C, Character> node) {
		super(
			tab,
			node,
			s -> s.isEmpty() ? '\0' : s.charAt(0),
			s -> s.length() <= 1,
			c -> c == null || c == '\0' ? "" : String.valueOf(c)
		);
		getInputField().setMaxLength(1);
	}
}
