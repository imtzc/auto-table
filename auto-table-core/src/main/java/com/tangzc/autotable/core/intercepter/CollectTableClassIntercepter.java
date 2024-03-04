package com.tangzc.autotable.core.intercepter;

import java.util.Set;

/**
 * 表信息拦截器
 */
@FunctionalInterface
public interface CollectTableClassIntercepter {
    /**
     * 拦截器
     *
     * @param tableClasses 所有实体模型class
     */
    void intercept(final Set<Class<?>> tableClasses);
}
