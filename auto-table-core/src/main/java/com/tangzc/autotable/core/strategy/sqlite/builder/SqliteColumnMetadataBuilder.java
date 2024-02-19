package com.tangzc.autotable.core.strategy.sqlite.builder;

import com.tangzc.autotable.annotation.ColumnDefault;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.constants.DatabaseDialect;
import com.tangzc.autotable.core.converter.DatabaseTypeAndLength;
import com.tangzc.autotable.core.strategy.sqlite.data.SqliteColumnMetadata;
import com.tangzc.autotable.core.strategy.sqlite.SqliteTypeHelper;
import com.tangzc.autotable.core.utils.TableBeanUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

/**
 * 用于存放创建表的字段信息
 *
 * @author don
 */
@Slf4j
public class SqliteColumnMetadataBuilder {

    public static SqliteColumnMetadata build(Class<?> clazz, Field field) {
        SqliteColumnMetadata sqliteColumnMetadata = new SqliteColumnMetadata();
        sqliteColumnMetadata.setName(TableBeanUtils.getRealColumnName(clazz, field));
        sqliteColumnMetadata.setType(getAndLength(clazz, field));
        sqliteColumnMetadata.setNotNull(TableBeanUtils.isNotNull(field, clazz));
        sqliteColumnMetadata.setPrimary(TableBeanUtils.isPrimary(field, clazz));
        sqliteColumnMetadata.setAutoIncrement(TableBeanUtils.isAutoIncrement(field, clazz));
        ColumnDefault columnDefault = TableBeanUtils.getDefaultValue(field);
        if (columnDefault != null) {
            sqliteColumnMetadata.setDefaultValueType(columnDefault.type());
            String defaultValue = columnDefault.value();
            Class<?> fieldType = field.getType();
            // 补偿逻辑：类型为Boolean的时候(实际数据库为bit数字类型)，兼容 true、false
            boolean isBooleanType = (fieldType == Boolean.class || fieldType == boolean.class) && SqliteTypeHelper.isInteger(sqliteColumnMetadata.getType());
            if (isBooleanType && !"1".equals(defaultValue) && !"0".equals(defaultValue)) {
                if (Boolean.parseBoolean(defaultValue)) {
                    defaultValue = "1";
                } else {
                    defaultValue = "0";
                }
            }
            // 补偿逻辑：字符串类型，前后自动添加'
            if (SqliteTypeHelper.isText(sqliteColumnMetadata.getType()) && !defaultValue.isEmpty() && !defaultValue.startsWith("'") && !defaultValue.endsWith("'")) {
                defaultValue = "'" + defaultValue + "'";
            }
            sqliteColumnMetadata.setDefaultValue(defaultValue);
        }
        sqliteColumnMetadata.setComment(TableBeanUtils.getComment(field));

        return sqliteColumnMetadata;
    }

    private static DatabaseTypeAndLength getAndLength(Class<?> clazz, Field field) {
        DatabaseTypeAndLength typeAndLength = AutoTableGlobalConfig.getJavaTypeToDatabaseTypeConverter().convert(DatabaseDialect.SQLITE, clazz, field);
        // 纠正类型的写法为正规方式
        String type = typeAndLength.getType().toLowerCase();
        if (type.contains("int")) {
            type = "integer";
        }
        if (type.contains("char") || type.contains("clob") || type.contains("text")) {
            type = "text";
        }
        if (type.contains("blob")) {
            type = "blob";
        }
        if (type.contains("real") || type.contains("floa") || type.contains("doub")) {
            type = "real";
        }
        typeAndLength.setType(type);
        return typeAndLength;
    }
}
