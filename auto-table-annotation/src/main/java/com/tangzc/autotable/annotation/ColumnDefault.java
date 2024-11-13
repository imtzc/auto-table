package com.tangzc.autotable.annotation;

import com.tangzc.autotable.annotation.enums.DefaultValueEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 字段的默认值
 * @author don
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ColumnDefault {

    /**
     * @return 列的默认值类型
     */
    DefaultValueEnum type() default DefaultValueEnum.UNDEFINED;

    /**
     * @return 列的默认值
     */
    String value() default "";
}
