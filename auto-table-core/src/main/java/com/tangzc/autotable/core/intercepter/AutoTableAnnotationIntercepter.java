package com.tangzc.autotable.core.intercepter;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * 自动表注解扫描拦截器
 * <p>注解收集完后，会根据两个注解集合扫描指定的包下的类，本拦截器发生在扫描包之前。
 * <p>因此，这里可以最终决定扫描哪些注解，排除哪些注解
 */
@FunctionalInterface
public interface AutoTableAnnotationIntercepter {

    /**
     * 拦截器，包含和排斥取交集，即：如果既包含又排斥，则会排除
     *
     * @param includeAnnotations 包含的注解
     * @param excludeAnnotations 排除的注解
     */
    void intercept(Set<Class<? extends Annotation>> includeAnnotations, Set<Class<? extends Annotation>> excludeAnnotations);
}
