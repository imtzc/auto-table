package org.dromara.autotable.annotation.mysql;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指定MySQL字符编码和排序规则
 * @author don
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MysqlCharset {

    /**
     * @return 字符集
     */
    String charset();

    /**
     * @return 字符排序
     */
    String collate();
}
