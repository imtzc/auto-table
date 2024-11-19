package com.tangzc.autotable.core.callback;

import java.util.Set;

/**
 * AutoTable执行结束的回调
 */
public interface AutoTableFinishCallback {

    /**
     * 执行结束，可以做一些数据相关的初始化工作
     *
     * @param tableClasses 实体模型class
     */
    void finish(final Set<Class<?>> tableClasses);
}
