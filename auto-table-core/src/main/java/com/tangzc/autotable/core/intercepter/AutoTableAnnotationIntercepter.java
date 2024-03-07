package com.tangzc.autotable.core.intercepter;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * 自动表注解拦截器
 */
@FunctionalInterface
public interface AutoTableAnnotationIntercepter {

    void intercept(Set<Class<? extends Annotation>> includeAnnotations, Set<Class<? extends Annotation>> excludeAnnotations);
}
