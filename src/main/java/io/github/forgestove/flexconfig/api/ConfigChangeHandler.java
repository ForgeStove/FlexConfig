package io.github.forgestove.flexconfig.api;
/**
 * 须有无参构造；实例由框架缓存复用，不可持有单次调用的状态；同一次保存内按类去重，至多调用一次。
 * 调用时配置对象已写入新值、落盘在其后；抛出的异常被吞掉并记日志。
 */
@FunctionalInterface
public interface ConfigChangeHandler {
	void onChange(Object oldValue, Object newValue);
}
