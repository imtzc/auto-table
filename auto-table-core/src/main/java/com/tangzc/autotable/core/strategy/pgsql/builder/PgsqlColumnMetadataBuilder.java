package com.tangzc.autotable.core.strategy.pgsql.builder;

import com.tangzc.autotable.annotation.ColumnDefault;
import com.tangzc.autotable.annotation.ColumnType;
import com.tangzc.autotable.annotation.enums.DefaultValueEnum;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.strategy.pgsql.JavaToPgsqlConverter;
import com.tangzc.autotable.core.strategy.pgsql.data.PgsqlColumnMetadata;
import com.tangzc.autotable.core.strategy.pgsql.data.PgsqlTypeAndLength;
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
public class PgsqlColumnMetadataBuilder {

    public static PgsqlColumnMetadata build(Class<?> clazz, Field field) {

        PgsqlColumnMetadata pgsqlColumnMetadata = new PgsqlColumnMetadata();
        pgsqlColumnMetadata.setName(TableBeanUtils.getRealColumnName(clazz, field));
        pgsqlColumnMetadata.setType(getTypeAndLength(field, clazz));
        pgsqlColumnMetadata.setNotNull(TableBeanUtils.isNotNull(field, clazz));
        pgsqlColumnMetadata.setPrimary(TableBeanUtils.isPrimary(field, clazz));
        pgsqlColumnMetadata.setAutoIncrement(TableBeanUtils.isAutoIncrement(field, clazz));
        ColumnDefault columnDefault = TableBeanUtils.getDefaultValue(field);
        if (columnDefault != null) {
            pgsqlColumnMetadata.setDefaultValueType(columnDefault.type());
            String defaultValue = columnDefault.value();
            if(StringUtils.hasText(defaultValue)) {
                PgsqlTypeAndLength type = pgsqlColumnMetadata.getType();
                // 补偿逻辑：字符串类型，前后自动添加'
                if (type.isCharString() && !defaultValue.isEmpty() && !defaultValue.startsWith("'") && !defaultValue.endsWith("'")) {
                    defaultValue = "'" + defaultValue + "'";
                }
                pgsqlColumnMetadata.setDefaultValue(defaultValue);
            }
        }
        pgsqlColumnMetadata.setComment(TableBeanUtils.getComment(field));

        return pgsqlColumnMetadata;
    }

    private static PgsqlTypeAndLength getTypeAndLength(Field field, Class<?> clazz) {

        // 类型为空根据字段类型去默认匹配类型
        Class<?> fieldType = TableBeanUtils.getFieldType(clazz, field);
        // 获取外部注入的自定义
        JavaToPgsqlConverter javaToPgsqlConverter = AutoTableGlobalConfig.getJavaToPgsqlConverter();
        PgsqlTypeAndLength typeAndLength = javaToPgsqlConverter.convert(clazz, field);

        ColumnType column = TableBeanUtils.getColumnType(field);
        if (column != null) {
            // 如果重新设置了类型，则长度也需要重新设置
            if (StringUtils.hasText(column.value()) && !column.value().equalsIgnoreCase(typeAndLength.getType())) {
                typeAndLength.setType(column.value());
                typeAndLength.setLength(null);
                typeAndLength.setDecimalLength(null);
            }
            if (column.length() >= 0) {
                typeAndLength.setLength(column.length());
            }
            if (column.decimalLength() >= 0) {
                typeAndLength.setDecimalLength(column.decimalLength());
            }
        }

        return typeAndLength;
    }
}
