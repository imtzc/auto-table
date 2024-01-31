package com.tangzc.autotable.annotation.mysql;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定MySQL字符编码和排序规则
 * @author don
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MysqlCharset {

    /**
     * 字符集
     */
    String charset();

    /**
     * 字符排序
     */
    String collate();
}
