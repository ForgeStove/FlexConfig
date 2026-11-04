package io.github.forgestove.flexconfig.client.gui;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.flexconfig.client.Translation;
import io.github.forgestove.flexconfig.client.gui.entry.*;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.awt.Point;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.*;
@SuppressWarnings("unused")
public class EntryTypeRegistry {
	private static final Map<Class<?>, EntryTypeRegistry> custom = new HashMap<>();
	private static final EntryTypeRegistry defaultRegistry = new EntryTypeRegistry();
	static {
		register(Boolean.class, BooleanValueConfigEntry::new);
		register(
			Integer.class, (tab, node) -> {
				if (node.isColorValue()) return new ColorValueConfigEntry<>(tab, node);
				if (node.getSliderRange() != null) return new SliderValueConfigEntry<>(tab, node, d -> (int) Math.round(d));
				return new IntegerValueConfigEntry<>(tab, node);
			}
		);
		register(
			Long.class,
			(tab, node) -> node.getSliderRange() != null
				? new SliderValueConfigEntry<>(tab, node, Math::round)
				: new LongValueConfigEntry<>(tab, node)
		);
		register(
			Float.class,
			(tab, node) -> node.getSliderRange() != null
				? new SliderValueConfigEntry<>(tab, node, d -> (float) d)
				: new FloatValueConfigEntry<>(tab, node)
		);
		register(
			Double.class,
			(tab, node) -> node.getSliderRange() != null
				? new SliderValueConfigEntry<>(tab, node, d -> d)
				: new DoubleValueConfigEntry<>(tab, node)
		);
		register(Byte.class, ByteValueConfigEntry::new);
		register(Short.class, ShortValueConfigEntry::new);
		register(Character.class, CharacterValueConfigEntry::new);
		registerEnumEntry();
		register(String.class, StringValueConfigEntry::new);
		register(Key.class, KeybindValueConfigEntry::new);
		register(Point.class, PointValueConfigEntry::new);
	}
	/** 枚举类型运行期才确定，注册入口在这里集中做一次裸类型桥接。 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	private static void registerEnumEntry() {
		register(Enum.class, (tab, node) -> new EnumValueConfigEntry(tab, node));
	}
	private final List<FieldProvider> annotationProviders = new ArrayList<>();
	private final List<FieldProvider> predicateProviders = new ArrayList<>();
	private final List<TypeProvider> typeProviders = new ArrayList<>();
	private final List<FieldTransformer> transformers = new ArrayList<>();
	private EntryTypeRegistry() {}
	public static EntryTypeRegistry get(Class<?> configClass) {
		return custom.computeIfAbsent(configClass, ignored -> new EntryTypeRegistry());
	}
	@SuppressWarnings("unchecked")
	public static <C, V> void register(
		Class<? super V> type,
		BiFunction<ConfigCategoryTab<C, V>, ValueConfigNode<C, V>, ConfigEntry> factory
	) {
		defaultRegistry.typeProviders.add(new TypeProvider(
			type,
			(tab, node) -> List.of(factory.apply((ConfigCategoryTab<C, V>) tab, (ValueConfigNode<C, V>) node))
		));
	}
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static List<ConfigEntry> createValueEntries(ConfigCategoryTab<?, ?> tab, ValueConfigNode<?, ?> node) {
		var registry = custom.get(tab.config.getClass());
		List<ConfigEntry> entries = registry == null ? null : registry.find(tab, node);
		// 集合由元素行组成；自定义提供器仍可优先覆盖
		if (entries == null && List.class.isAssignableFrom(node.getValueType()))
			entries = CollectionValueConfigEntry.create((ConfigCategoryTab) tab, (ValueConfigNode) node);
		if (entries == null) entries = defaultRegistry.find(tab, node);
		if (entries == null) entries = List.of(new TextConfigEntry(Translation.UNSUPPORTED_TYPE.copy()
			.append(node.getValueType().getSimpleName())
			.withStyle(ChatFormatting.RED)));
		if (registry != null) entries = registry.transform(entries, tab, node);
		return defaultRegistry.transform(entries, tab, node);
	}
	public static ConfigEntry createCategoryEntry(Component label, boolean expanded, int depth, Runnable onToggle) {
		return new CategoryCollapsibleConfigEntry(label, expanded, depth, onToggle);
	}
	@SafeVarargs
	public final void registerAnnotationProvider(EntryProvider provider, Class<? extends Annotation>... annotations) {
		for (var annotation : annotations)
			annotationProviders.add(new FieldProvider(field -> field.isAnnotationPresent(annotation), provider));
	}
	public void registerPredicateProvider(EntryProvider provider, Predicate<Field> predicate) {
		predicateProviders.add(new FieldProvider(predicate, provider));
	}
	public void registerTypeProvider(EntryProvider provider, Class<?>... types) {
		for (var type : types) typeProviders.add(new TypeProvider(type, provider));
	}
	@SafeVarargs
	public final void registerAnnotationTransformer(EntryTransformer transformer, Class<? extends Annotation>... annotations) {
		for (var annotation : annotations)
			transformers.add(new FieldTransformer(field -> field.isAnnotationPresent(annotation), transformer));
	}
	public void registerPredicateTransformer(EntryTransformer transformer, Predicate<Field> predicate) {
		transformers.add(new FieldTransformer(predicate, transformer));
	}
	private @Nullable List<ConfigEntry> find(ConfigCategoryTab<?, ?> tab, ValueConfigNode<?, ?> node) {
		var field = node.getField();
		if (field != null) {
			for (var entry : annotationProviders)
				if (entry.predicate().test(field)) return entry.provider().create(tab, node);
			for (var entry : predicateProviders)
				if (entry.predicate().test(field)) return entry.provider().create(tab, node);
		}
		for (var entry : typeProviders)
			if (entry.type().isAssignableFrom(node.getValueType())) return entry.provider().create(tab, node);
		return null;
	}
	private List<ConfigEntry> transform(List<ConfigEntry> entries, ConfigCategoryTab<?, ?> tab, ValueConfigNode<?, ?> node) {
		var field = node.getField();
		if (field == null) return entries;
		for (var entry : transformers)
			if (entry.predicate().test(field)) entries = entry.transformer().transform(entries, tab, node);
		return entries;
	}
	@FunctionalInterface
	public interface EntryProvider {
		@Nullable List<ConfigEntry> create(ConfigCategoryTab<?, ?> tab, ValueConfigNode<?, ?> node);
	}
	@FunctionalInterface
	public interface EntryTransformer {
		List<ConfigEntry> transform(List<ConfigEntry> entries, ConfigCategoryTab<?, ?> tab, ValueConfigNode<?, ?> node);
	}
	private record FieldProvider(Predicate<Field> predicate, EntryProvider provider) {}
	private record TypeProvider(Class<?> type, EntryProvider provider) {}
	private record FieldTransformer(Predicate<Field> predicate, EntryTransformer transformer) {}
}
