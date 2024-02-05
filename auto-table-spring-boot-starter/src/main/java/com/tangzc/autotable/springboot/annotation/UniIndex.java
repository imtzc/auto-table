package com.tangzc.autotable.springboot.annotation;

import com.tangzc.autotable.annotation.Index;
import com.tangzc.autotable.annotation.enums.IndexTypeEnum;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Index(type = IndexTypeEnum.UNIQUE)
public @interface UniIndex {

    /**
     * 索引的名字，不设置默认为{mpe_idx_当前标记字段名@Column的name}<p>
     * 如果设置了名字例如union_name,系统会默认在名字前加mpe_idx_前缀，也就是mpe_idx_union_name
     */
    @AliasFor(annotation = Index.class, attribute = "name")
    String name() default "";

    /**
     * 索引注释
     */
    @AliasFor(annotation = Index.class, attribute = "comment")
    String comment() default "";
}
