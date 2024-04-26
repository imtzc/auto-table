package com.tangzc.autotable.core;

import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.autotable.annotation.ColumnName;
import com.tangzc.autotable.annotation.TableName;
import com.tangzc.autotable.core.utils.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
    boolean isIgnoreField(Field field, Class<?> clazz);

    /**
     * 判断是否是主键
     *
     * @param field 字段
     * @param clazz 类
     * @return 是否是主键
     */
    boolean isPrimary(Field field, Class<?> clazz);

    /**
     * 判断是否是自增的主键
     *
     * @param field 字段
     * @param clazz 类
     * @return 是否是自增的主键
     */
    boolean isAutoIncrement(Field field, Class<?> clazz);

    /**
     * 三方框架定义的字段类型，通常是某些特殊注解或者枚举，定义为字符串类型
     *
     * @param field 字段
     * @param clazz 类
     * @return 该字段的类型
     */
    default Class<?> customFieldTypeHandler(Class<?> clazz, Field field) {
        return field.getType();
    }

    /**
     * 获取枚举值，默认是枚举的名字
     *
     * @param enumType 枚举类型
     * @return 该枚举下的所有追
     */
    default List<String> getEnumValues(Class<?> enumType) {
        if (enumType.isEnum()) {
            return Arrays.stream(enumType.getEnumConstants()).map(Object::toString).collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException(String.format("Class: %s 非枚举类型", enumType.getName()));
        }
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

        String tableName = null;

        AutoTableAnnotationFinder autoTableAnnotationFinder = AutoTableGlobalConfig.getAutoTableAnnotationFinder();

        // TODO 将要删除的逻辑，仅供兼容
        TableName tableNameAnno = autoTableAnnotationFinder.find(clazz, TableName.class);
        if (tableNameAnno != null && StringUtils.hasText(tableNameAnno.value())) {
            tableName = tableNameAnno.value();
        }

        AutoTable autoTable = autoTableAnnotationFinder.find(clazz, AutoTable.class);
        if (autoTable != null && StringUtils.hasText(autoTable.value())) {
            tableName = autoTable.value();
        }

        if (tableName == null) {
            tableName = StringUtils.camelToUnderline(clazz.getSimpleName());
        }

        return tableName;
    }

    /**
     * 获取表schema
     *
     * @param clazz 实体类
     * @return 表schema
     */
    default String getTableSchema(Class<?> clazz) {

        AutoTableAnnotationFinder autoTableAnnotationFinder = AutoTableGlobalConfig.getAutoTableAnnotationFinder();
        AutoTable autoTable = autoTableAnnotationFinder.find(clazz, AutoTable.class);
        if(autoTable != null) {
            return autoTable.schema();
        }
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
        ColumnName columnNameAnno = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(field, ColumnName.class);
        if (columnNameAnno != null) {
            String columnName = columnNameAnno.value();
            if (StringUtils.hasText(columnName)) {
                return columnName;
            }
        }
        return StringUtils.camelToUnderline(field.getName());
    }


    class DefaultAutoTableOrmFrameAdapter implements AutoTableOrmFrameAdapter {
        @Override
        public boolean isIgnoreField(Field field, Class<?> clazz) {
            // 默认所有字段均不被排除
            return false;
        }

        @Override
        public boolean isPrimary(Field field, Class<?> clazz) {
            // 默认不是主键
            return false;
        }

        @Override
        public boolean isAutoIncrement(Field field, Class<?> clazz) {
            // 默认都不是自增的
            return false;
        }
    }
}
