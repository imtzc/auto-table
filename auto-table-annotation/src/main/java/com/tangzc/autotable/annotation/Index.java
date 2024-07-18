package com.tangzc.autotable.annotation;

import com.tangzc.autotable.annotation.enums.IndexTypeEnum;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 设置字段索引
 *
 * @author don
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Index {

    /**
     * 索引的名字，不设置默认为{auto_idx_[表名]_[字段名]}
     * <p>生成规则优化：
     * <p>1. 优先使用 auto_idx_`[表名]`_`[字段名1]`_`[字段名2]`
     * <p>2. 若超长(63字符)了，使用 auto_idx_`[表名]`_`[所有字段名链接后的hash值]`
     * <p>   > 注：长度定义63是兼容了pgsql的63字符，与mysql的64字符考虑的，Oracle本就不打算兼容，所以不考虑它的30字符长度
     * <p>3. 若仍超长了，使用 auto_idx_`[表名+所有字段名链接后的hash值]`
     */
    String name() default "";

    /**
     * 索引类型
     */
    IndexTypeEnum type() default IndexTypeEnum.NORMAL;

    /**
     * 索引注释
     */
    String comment() default "";

}

