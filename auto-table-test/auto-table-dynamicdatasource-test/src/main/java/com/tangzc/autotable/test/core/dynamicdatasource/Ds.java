package com.tangzc.autotable.test.core.dynamicdatasource;

import com.tangzc.autotable.annotation.TableIndexes;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Ds {
    String value();
}
