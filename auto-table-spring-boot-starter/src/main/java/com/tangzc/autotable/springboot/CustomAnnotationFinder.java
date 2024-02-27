package com.tangzc.autotable.springboot;

import com.tangzc.autotable.core.AutoTableAnnotationFinder;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CustomAnnotationFinder implements AutoTableAnnotationFinder {
    @Override
    public <A extends Annotation> A find(Class<?> clazz, Class<A> annotationClass) {
        return AnnotatedElementUtils.getMergedAnnotation(clazz, annotationClass);
    }

    @Override
    public <A extends Annotation> A find(Method method, Class<A> annotationClass) {
        return AnnotatedElementUtils.getMergedAnnotation(method, annotationClass);
    }

    @Override
    public <A extends Annotation> A find(Field field, Class<A> annotationClass) {
        return AnnotatedElementUtils.getMergedAnnotation(field, annotationClass);
    }

    @Override
    public <A extends Annotation> boolean exist(Class<?> clazz, Class<A> annotationClass) {
        return AnnotatedElementUtils.isAnnotated(clazz, annotationClass);
    }

    @Override
    public <A extends Annotation> boolean exist(Method method, Class<A> annotationClass) {
        return AnnotatedElementUtils.isAnnotated(method, annotationClass);
    }

    @Override
    public <A extends Annotation> boolean exist(Field field, Class<A> annotationClass) {
        return AnnotatedElementUtils.isAnnotated(field, annotationClass);
    }
}
