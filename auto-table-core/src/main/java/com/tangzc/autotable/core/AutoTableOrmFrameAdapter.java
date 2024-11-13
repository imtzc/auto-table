package com.tangzc.autotable.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

/**
 * @author don
 */
public interface AutoTableOrmFrameAdapter {

    /**
     * 拓展判断是否是忽略的字段
     *
     * @param field 字段
     * @param clazz 类
     * @return 是否忽略
     */
    default boolean isIgnoreField(Field field, Class<?> clazz) {
        // 默认所有字段均不被排除
        return false;
    }

    /**
     * 判断是否是主键
     *
     * @param field 字段
     * @param clazz 类
     * @return 是否是主键
     */
    default boolean isPrimary(Field field, Class<?> clazz) {
        // 默认不是主键
        return false;
    }

    /**
     * 判断是否是自增的主键
     *
     * @param field 字段
     * @param clazz 类
     * @return 是否是自增的主键
     */
    default boolean isAutoIncrement(Field field, Class<?> clazz) {
        // 默认都不是自增的
        return false;
    }

    /**
     * 三方框架定义的字段类型，通常是某些特殊注解或者枚举，定义为字符串类型
     *
     * @param field 字段
     * @param clazz 类
     * @return 该字段的类型
     */
    default Class<?> customFieldTypeHandler(Class<?> clazz, Field field) {
        return null;
    }

    /**
     * 获取枚举值，默认是枚举的名字
     *
     * @param enumType 枚举类型
     * @return 该枚举下的所有追
     */
    default List<String> getEnumValues(Class<?> enumType) {
        return Collections.emptyList();
    }

    /**
     * 扫描注解
     *
     * @return 扫描的注解集合
     */
    default List<Class<? extends Annotation>> scannerAnnotations() {
        return Collections.emptyList();
    }

    /**
     * 获取表名
     *
     * @param clazz 实体类
     * @return 表名
     */
    default String getTableName(Class<?> clazz) {
        return null;
    }

    /**
     * 获取表注释
     *
     * @param clazz 实体类
     * @return 标注释
     */
    default String getTableComment(Class<?> clazz) {
        return null;
    }

    /**
     * 获取表schema
     *
     * @param clazz 实体类
     * @return 表schema
     */
    default String getTableSchema(Class<?> clazz) {
        return null;
    }

    /**
     * 获取字段名
     *
     * @param clazz 实体类
     * @param field 字段
     * @return 字段名
     */
    default String getRealColumnName(Class<?> clazz, Field field) {
        return null;
    }

    /**
     * 获取字段注释
     *
     * @param field 字段
     * @param clazz 实体类
     * @return 字段注释
     */
    default String getColumnComment(Field field, Class<?> clazz) {
        return null;
    }
}
