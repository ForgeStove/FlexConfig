package io.github.forgestove.flexconfig.api;
import io.github.forgestove.flexconfig.platform.Platform;

import java.lang.reflect.Field;
import java.util.Arrays;
/** 实现类须有无参构造（框架按无参构造实例化）。 */
@FunctionalInterface
public interface ConfigCondition {
	/** 无 {@link Condition} 注解的字段视为通过。 */
	static boolean evaluate(Field field) {
		var condition = field.getAnnotation(Condition.class);
		return condition == null || evaluate(condition);
	}
	static boolean evaluate(Condition condition) {
		var mod = condition.value();
		if (!mod.isEmpty() && !Platform.get().isModLoaded(mod)) return false;
		return Arrays.stream(condition.condition()).allMatch(ConfigCondition::evaluate);
	}
	private static boolean evaluate(Class<? extends ConfigCondition> conditionClass) {
		try {
			return conditionClass.getDeclaredConstructor().newInstance().test();
		} catch (Exception e) {
			throw new RuntimeException("Failed to evaluate config condition '%s'".formatted(conditionClass.getSimpleName()), e);
		}
	}
	boolean test();
}
