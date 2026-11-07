package io.github.forgestove.flexconfig.api.registration;
/**
 * 实现类要有无参构造；实例由框架缓存复用，不能保存单次调用的状态；同一次保存内按类去重，最多调用一次。
 * 调用时配置对象已经写入新值，文件要等调用结束才写入；抛出的异常会被吞掉并记入日志。
 */
@FunctionalInterface
public interface ConfigChangeHandler {
	void onChange(Object oldValue, Object newValue);
}
