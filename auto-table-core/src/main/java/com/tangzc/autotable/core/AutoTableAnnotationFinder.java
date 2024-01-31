package com.tangzc.autotable.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public interface AutoTableAnnotationFinder {

    default <A extends Annotation> A find(Class<?> clazz, Class<A> annotationClass) {
        return clazz.getDeclaredAnnotation(annotationClass);
    }
    default <A extends Annotation> A find(Method method, Class<A> annotationClass) {
        return method.getDeclaredAnnotation(annotationClass);
    }
    default <A extends Annotation> A find(Field field, Class<A> annotationClass) {
        return field.getDeclaredAnnotation(annotationClass);
    }

    default <A extends Annotation> boolean exist(Class<?> clazz, Class<A> annotationClass) {
        return find(clazz, annotationClass) != null;
    }
    default <A extends Annotation> boolean exist(Method method, Class<A> annotationClass) {
        return find(method, annotationClass) != null;
    }
    default <A extends Annotation> boolean exist(Field field, Class<A> annotationClass) {
        return find(field, annotationClass) != null;
    }
}
