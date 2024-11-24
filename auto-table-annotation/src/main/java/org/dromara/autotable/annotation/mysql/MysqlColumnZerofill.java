package org.dromara.autotable.annotation.mysql;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>指定MySQL数字类型进行填充0
 * <p>举例：
 * <p>CREATE TABLE example (
 * <p>    id INT(5) ZEROFILL
 * <p>);
 * <p>INSERT INTO example (id) VALUES (123); -- 查询结果会显示为 00123
 *
 * @author don
 */
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MysqlColumnZerofill {

}
