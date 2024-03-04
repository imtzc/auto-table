package com.tangzc.autotable.core.callback;

import java.util.Set;

/**
 * 运行回调
 */
@FunctionalInterface
public interface RunFinishCallback {

    /**
     * 结束
     *
     * @param tableClasses 所有实体模型class
     */
    void finish(final Set<Class<?>> tableClasses);
}
