package org.dromara.autotable.annotation.mysql;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>指定MySQL数字类型不允许负数，其范围从 0 开始
 * <p>举例：
 * <p>CREATE TABLE example (
 * <p>    name INT UNSIGNED   -- INT UNSIGNED 范围是：0 到 4294967295
 * <p>);
 *
 * @author don
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MysqlColumnUnsigned {

}
