package com.tangzc.autotable.core.intercepter;

import java.util.Set;

/**
 * 收集模型实体集合的拦截器
 */
@FunctionalInterface
public interface CollectEntitiesIntercepter {
    /**
     * 拦截器
     *
     * @param entityClass 所有实体模型class
     */
    void intercept(final Set<Class<?>> entityClass);
}
