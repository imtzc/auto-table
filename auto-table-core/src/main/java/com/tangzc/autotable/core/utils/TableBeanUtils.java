package com.tangzc.autotable.core.utils;

import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.autotable.annotation.ColumnComment;
import com.tangzc.autotable.annotation.ColumnDefault;
import com.tangzc.autotable.annotation.ColumnName;
import com.tangzc.autotable.annotation.ColumnNotNull;
import com.tangzc.autotable.annotation.ColumnType;
import com.tangzc.autotable.annotation.Ignore;
import com.tangzc.autotable.annotation.Index;
import com.tangzc.autotable.annotation.PrimaryKey;
import com.tangzc.autotable.annotation.TableComment;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.autotable.annotation.TableIndexes;
import com.tangzc.autotable.annotation.TableName;
import com.tangzc.autotable.core.AutoTableAnnotationFinder;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.AutoTableOrmFrameAdapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author don
 */
public class TableBeanUtils {

    public static boolean isIncludeField(Field field, Class<?> clazz) {

        Ignore ignore = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(field, Ignore.class);
        if (ignore != null) {
            return false;
        }

        // 调用第三方ORM实现
        boolean isIgnoreField = AutoTableGlobalConfig.getAutoTableOrmFrameAdapter().isIgnoreField(field, clazz);
        return !isIgnoreField;
    }

    public static List<TableIndex> getTableIndexes(Class<?> clazz) {
        List<TableIndex> tableIndices = new ArrayList<>();
        TableIndexes tableIndexes = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(clazz, TableIndexes.class);
        if (tableIndexes != null) {
            Collections.addAll(tableIndices, tableIndexes.value());
        }
        TableIndex tableIndex = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(clazz, TableIndex.class);
        if (tableIndex != null) {
            tableIndices.add(tableIndex);
        }
        return tableIndices;
    }

    /**
     * 获取bean上的表名
     *
     * @param clazz bean
     * @return 表名
     */
    public static String getTableName(Class<?> clazz) {

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

        // 调用第三方ORM实现
        if (tableName == null) {
            tableName = AutoTableGlobalConfig.getAutoTableOrmFrameAdapter().getTableName(clazz);
        }

        if (tableName == null) {
            tableName = StringUtils.camelToUnderline(clazz.getSimpleName());
        }

        return tableName;
    }

    /**
     * 获取bean上的schema
     * @param clazz bean
     * @return schema
     */
    public static String getTableSchema(Class<?> clazz) {

        AutoTableAnnotationFinder autoTableAnnotationFinder = AutoTableGlobalConfig.getAutoTableAnnotationFinder();
        AutoTable autoTable = autoTableAnnotationFinder.find(clazz, AutoTable.class);
        if(autoTable != null) {
            return autoTable.schema();
        }

        // 调用第三方ORM实现
        return AutoTableGlobalConfig.getAutoTableOrmFrameAdapter().getTableSchema(clazz);
    }

    public static String getTableComment(Class<?> clazz) {
        AutoTableAnnotationFinder autoTableAnnotationFinder = AutoTableGlobalConfig.getAutoTableAnnotationFinder();

        TableComment tableComment = autoTableAnnotationFinder.find(clazz, TableComment.class);
        if(tableComment != null) {
            return tableComment.value();
        }

        AutoTable autoTable = autoTableAnnotationFinder.find(clazz, AutoTable.class);
        if(autoTable != null) {
            return autoTable.comment();
        }
        return null;
    }

    public static boolean isPrimary(Field field, Class<?> clazz) {

        PrimaryKey isPrimary = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(field, PrimaryKey.class);
        if (isPrimary != null) {
            return true;
        }

        // 调用第三方ORM实现
        return AutoTableGlobalConfig.getAutoTableOrmFrameAdapter().isPrimary(field, clazz);
    }

    public static boolean isAutoIncrement(Field field, Class<?> clazz) {
        PrimaryKey isPrimary = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(field, PrimaryKey.class);
        if (isPrimary != null) {
            return isPrimary.value();
        }
        AutoTableOrmFrameAdapter autoTableOrmFrameAdapter = AutoTableGlobalConfig.getAutoTableOrmFrameAdapter();
        return autoTableOrmFrameAdapter.isAutoIncrement(field, clazz);
    }

    public static Boolean isNotNull(Field field, Class<?> clazz) {
        // 主键默认为非空
        if (isPrimary(field, clazz)) {
            return true;
        }

        ColumnNotNull column = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(field, ColumnNotNull.class);
        return column != null && column.value();
    }

    public static String getComment(Field field) {
        ColumnComment column = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(field, ColumnComment.class);
        if (column != null) {
            return column.value();
        }
        return "";
    }

    public static ColumnDefault getDefaultValue(Field field) {
        return AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(field, ColumnDefault.class);
    }

    public static ColumnType getColumnType(Field field) {
        return AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(field, ColumnType.class);
    }

    public static Index getIndex(Field field) {
        return AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(field, Index.class);
    }

    public static Class<?> getFieldType(Class<?> clazz, Field field) {

        // 自定义获取字段的类型
        Class<?> fieldType = AutoTableGlobalConfig.getAutoTableOrmFrameAdapter().customFieldTypeHandler(clazz, field);

        if (fieldType == null) {
            fieldType = field.getType();
        }

        return fieldType;
    }

    /**
     * 根据注解顺序和配置，获取字段对应的数据库字段名
     *
     * @param clazz bean
     * @param field 字段
     * @return 字段名
     */
    public static String getRealColumnName(Class<?> clazz, Field field) {

        ColumnName columnNameAnno = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(field, ColumnName.class);
        if (columnNameAnno != null) {
            String columnName = columnNameAnno.value();
            if (StringUtils.hasText(columnName)) {
                return columnName;
            }
        }

        String realColumnName = AutoTableGlobalConfig.getAutoTableOrmFrameAdapter().getRealColumnName(clazz, field);
        if(StringUtils.hasText(realColumnName)) {
            return realColumnName;
        }

        return StringUtils.camelToUnderline(field.getName());
    }

    /**
     * 根据注解顺序和配置，获取字段对应的数据库字段名
     *
     * @param beanClazz bean class
     * @param fieldName 字段名
     * @return 字段名
     */
    public static String getRealColumnName(Class<?> beanClazz, String fieldName) {

        Field field = BeanClassUtil.getField(beanClazz, fieldName);
        return getRealColumnName(beanClazz, field);
    }
}
