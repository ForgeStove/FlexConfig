package io.github.forgestove.flexconfig.api.registration;
import io.github.forgestove.flexconfig.api.annotation.Condition;
import io.github.forgestove.flexconfig.platform.FlexConfigServices;

import java.lang.reflect.Field;
import java.util.Arrays;
@FunctionalInterface
public interface ConfigCondition {
	static boolean evaluate(Field field) {
		var condition = field.getAnnotation(Condition.class);
		return condition == null || evaluate(condition);
	}
	static boolean evaluate(Condition condition) {
		var mod = condition.value();
		if (!mod.isEmpty() && !FlexConfigServices.PLATFORM.isModLoaded(mod)) return false;
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
