package com.tangzc.autotable.core.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

/**
 * 注解合并处理类，可以将相同的注解的不同实例，中的属性合并为一个注解实例
 *
 * @author don
 */
@Slf4j
public class AnnotatedElementUtilsPlus {

    public static <ANNO extends Annotation> ANNO findDeepMergedAnnotation(AnnotatedElement element, Class<ANNO> annoClass) {
        return element.getAnnotation(annoClass);
    }
}
