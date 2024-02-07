package com.tangzc.autotable.core.strategy.sqlite.builder;

import com.tangzc.autotable.annotation.ColumnDefault;
import com.tangzc.autotable.annotation.ColumnType;
import com.tangzc.autotable.annotation.enums.DefaultValueEnum;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.strategy.sqlite.JavaToSqliteConverter;
import com.tangzc.autotable.core.strategy.sqlite.data.SqliteColumnMetadata;
import com.tangzc.autotable.core.strategy.sqlite.data.SqliteTypeAndLength;
import com.tangzc.autotable.core.utils.StringConnectHelper;
import com.tangzc.autotable.core.utils.StringUtils;
import com.tangzc.autotable.core.utils.TableBeanUtils;
import lombok.Data;
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
        ColumnType columnType = TableBeanUtils.getColumnType(field);
        if (columnType != null && StringUtils.hasText(columnType.value())) {
            SqliteTypeAndLength sqliteTypeAndLength = new SqliteTypeAndLength(columnType.length(), columnType.decimalLength(), columnType.value());
            sqliteColumnMetadata.setType(sqliteTypeAndLength);
        } else {
            Class<?> fieldType = TableBeanUtils.getFieldType(clazz, field);
            JavaToSqliteConverter javaToSqliteConverter = AutoTableGlobalConfig.getJavaToSqliteConverter();
            sqliteColumnMetadata.setType(javaToSqliteConverter.convert(clazz, field));
        }
        sqliteColumnMetadata.setNotNull(TableBeanUtils.isNotNull(field, clazz));
        sqliteColumnMetadata.setPrimary(TableBeanUtils.isPrimary(field, clazz));
        sqliteColumnMetadata.setAutoIncrement(TableBeanUtils.isAutoIncrement(field, clazz));
        ColumnDefault columnDefault = TableBeanUtils.getDefaultValue(field);
        if (columnDefault != null) {
            sqliteColumnMetadata.setDefaultValueType(columnDefault.type());
            String defaultValue = columnDefault.value();
            Class<?> fieldType = field.getType();
            // 补偿逻辑：类型为Boolean的时候(实际数据库为bit数字类型)，兼容 true、false
            boolean isBooleanType = (fieldType == Boolean.class || fieldType == boolean.class) && sqliteColumnMetadata.getType().isInteger();
            if (isBooleanType && !"1".equals(defaultValue) && !"0".equals(defaultValue)) {
                if (Boolean.parseBoolean(defaultValue)) {
                    defaultValue = "1";
                } else {
                    defaultValue = "0";
                }
            }
            // 补偿逻辑：字符串类型，前后自动添加'
            if (sqliteColumnMetadata.getType().isText() && !defaultValue.isEmpty() && !defaultValue.startsWith("'") && !defaultValue.endsWith("'")) {
                defaultValue = "'" + defaultValue + "'";
            }
            sqliteColumnMetadata.setDefaultValue(defaultValue);
        }
        sqliteColumnMetadata.setComment(TableBeanUtils.getComment(field));

        return sqliteColumnMetadata;
    }
}
