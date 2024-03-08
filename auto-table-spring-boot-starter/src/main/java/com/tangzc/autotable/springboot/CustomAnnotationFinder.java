package com.tangzc.autotable.springboot;

import com.tangzc.autotable.core.AutoTableAnnotationFinder;
import com.tangzc.autotable.springboot.util.AnnotatedElementUtilsPlus;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CustomAnnotationFinder implements AutoTableAnnotationFinder {
    @Override
    public <A extends Annotation> A find(Class<?> clazz, Class<A> annotationClass) {
        return AnnotatedElementUtilsPlus.getDeepMergedAnnotation(clazz, annotationClass);
    }

    @Override
    public <A extends Annotation> A find(Method method, Class<A> annotationClass) {
        return AnnotatedElementUtilsPlus.getDeepMergedAnnotation(method, annotationClass);
    }

    @Override
    public <A extends Annotation> A find(Field field, Class<A> annotationClass) {
        return AnnotatedElementUtilsPlus.getDeepMergedAnnotation(field, annotationClass);
    }

    @Override
    public <A extends Annotation> boolean exist(Class<?> clazz, Class<A> annotationClass) {
        return AnnotatedElementUtilsPlus.getDeepMergedAnnotation(clazz, annotationClass) != null;
    }

    @Override
    public <A extends Annotation> boolean exist(Method method, Class<A> annotationClass) {
        return AnnotatedElementUtilsPlus.getDeepMergedAnnotation(method, annotationClass) != null;
    }

    @Override
    public <A extends Annotation> boolean exist(Field field, Class<A> annotationClass) {
        return AnnotatedElementUtilsPlus.getDeepMergedAnnotation(field, annotationClass) != null;
    }
}
