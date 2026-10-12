package io.github.forgestove.flexconfig.api;
import io.github.forgestove.flexconfig.platform.Platform;

import java.lang.reflect.Field;
import java.util.Arrays;
/** 配置分类或字段加载时必须满足的条件，实现类须有无参构造。 */
@FunctionalInterface
public interface ConfigCondition {
	/** 没有 {@link Condition} 注解的字段始终通过。 */
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
	/** 条件是否满足。 */
	boolean test();
}
