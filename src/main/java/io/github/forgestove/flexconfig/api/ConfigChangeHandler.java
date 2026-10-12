package io.github.forgestove.flexconfig.api;
/**
 * 配置值变化后的副作用处理器（配合 {@link OnChange} 使用）。
 * <p>
 * 契约：须有无参构造，实例由框架缓存复用（别存每次调用的状态）；同一次保存内按类去重、至多调一次；
 * 调用时配置对象已写入新值、落盘在其后；抛出异常会被吞掉并记日志。
 */
@FunctionalInterface
public interface ConfigChangeHandler {
	void onChange(Object oldValue, Object newValue);
}
