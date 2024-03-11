package com.tangzc.autotable.core.callback;

/**
 * 单个表执行前后回调
 */
public interface RunStateCallback {

    /**
     * 执行前
     *
     * @param tableClass 实体模型class
     */
    void before(final Class<?> tableClass);

    /**
     * 执行后
     *
     * @param tableClass 实体模型class
     */
    void after(final Class<?> tableClass);
}
