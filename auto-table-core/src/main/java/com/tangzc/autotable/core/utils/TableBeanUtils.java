package com.tangzc.autotable.core.utils;

import com.tangzc.autotable.annotation.AutoColumn;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.autotable.annotation.ColumnComment;
import com.tangzc.autotable.annotation.ColumnDefault;
import com.tangzc.autotable.annotation.ColumnName;
import com.tangzc.autotable.annotation.ColumnNotNull;
import com.tangzc.autotable.annotation.ColumnType;
import com.tangzc.autotable.annotation.Ignore;
import com.tangzc.autotable.annotation.Index;
import com.tangzc.autotable.annotation.PrimaryKey;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.autotable.annotation.TableIndexes;
import com.tangzc.autotable.annotation.enums.DefaultValueEnum;
import com.tangzc.autotable.core.AutoTableAnnotationFinder;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.AutoTableOrmFrameAdapter;

import java.lang.annotation.Annotation;
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
     *
     * @param clazz bean
     * @return schema
     */
    public static String getTableSchema(Class<?> clazz) {

        AutoTableAnnotationFinder autoTableAnnotationFinder = AutoTableGlobalConfig.getAutoTableAnnotationFinder();
        AutoTable autoTable = autoTableAnnotationFinder.find(clazz, AutoTable.class);
        if (autoTable != null) {
            return autoTable.schema();
        }

        // 调用第三方ORM实现
        return AutoTableGlobalConfig.getAutoTableOrmFrameAdapter().getTableSchema(clazz);
    }

    public static String getTableComment(Class<?> clazz) {
        AutoTableAnnotationFinder autoTableAnnotationFinder = AutoTableGlobalConfig.getAutoTableAnnotationFinder();

        AutoTable autoTable = autoTableAnnotationFinder.find(clazz, AutoTable.class);
        if (autoTable != null) {
            return autoTable.comment();
        }

        AutoTableOrmFrameAdapter autoTableOrmFrameAdapter = AutoTableGlobalConfig.getAutoTableOrmFrameAdapter();
        String adapterTableComment = autoTableOrmFrameAdapter.getTableComment(clazz);
        if(adapterTableComment != null) {
            return adapterTableComment;
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
        if (column != null) {
            return column.value();
        }
        AutoColumn autoColumn = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(field, AutoColumn.class);
        if (autoColumn != null) {
            return autoColumn.notNull();
        }
        return false;
    }

    public static String getComment(Field field, Class<?> clazz) {
        ColumnComment column = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(field, ColumnComment.class);
        if (column != null) {
            return column.value();
        }
        AutoColumn autoColumn = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(field, AutoColumn.class);
        if (autoColumn != null) {
            return autoColumn.comment();
        }

        AutoTableOrmFrameAdapter autoTableOrmFrameAdapter = AutoTableGlobalConfig.getAutoTableOrmFrameAdapter();
        String adapterColumnComment = autoTableOrmFrameAdapter.getColumnComment(field, clazz);
        if(adapterColumnComment != null) {
            return adapterColumnComment;
        }

        return "";
    }

    public static ColumnDefault getDefaultValue(Field field) {
        ColumnDefault columnDefault = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(field, ColumnDefault.class);
        if (columnDefault != null) {
            return columnDefault;
        }
        AutoColumn autoColumn = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(field, AutoColumn.class);
        if (autoColumn != null) {
            return new ColumnDefault() {
                @Override
                public Class<? extends Annotation> annotationType() {
                    return ColumnDefault.class;
                }

                @Override
                public DefaultValueEnum type() {
                    return autoColumn.defaultValueType();
                }

                @Override
                public String value() {
                    return autoColumn.defaultValue();
                }
            };
        }
        return null;
    }

    public static ColumnType getColumnType(Field field) {
        ColumnType columnType = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(field, ColumnType.class);
        if (columnType != null) {
            return columnType;
        }

        AutoColumn autoColumn = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(field, AutoColumn.class);
        if (autoColumn != null) {
            return new ColumnType() {
                @Override
                public String value() {
                    return autoColumn.type();
                }

                @Override
                public int length() {
                    return autoColumn.length();
                }

                @Override
                public int decimalLength() {
                    return autoColumn.decimalLength();
                }

                @Override
                public String[] values() {
                    return new String[0];
                }

                @Override
                public Class<? extends Annotation> annotationType() {
                    return ColumnType.class;
                }
            };
        }
        return null;
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
        AutoColumn autoColumn = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(field, AutoColumn.class);
        if (autoColumn != null) {
            String columnName = autoColumn.value();
            if (StringUtils.hasText(columnName)) {
                return columnName;
            }
        }

        String realColumnName = AutoTableGlobalConfig.getAutoTableOrmFrameAdapter().getRealColumnName(clazz, field);
        if (StringUtils.hasText(realColumnName)) {
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
