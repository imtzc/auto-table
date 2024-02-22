package com.tangzc.autotable.core.strategy.pgsql.builder;

import com.tangzc.autotable.annotation.ColumnDefault;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.constants.DatabaseDialect;
import com.tangzc.autotable.core.converter.DatabaseTypeAndLength;
import com.tangzc.autotable.core.converter.JavaTypeToDatabaseTypeConverter;
import com.tangzc.autotable.core.strategy.pgsql.data.PgsqlColumnMetadata;
import com.tangzc.autotable.core.strategy.pgsql.data.PgsqlTypeHelper;
import com.tangzc.autotable.core.utils.StringUtils;
import com.tangzc.autotable.core.utils.TableBeanUtils;
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

        JavaTypeToDatabaseTypeConverter javaTypeToDatabaseTypeConverter = AutoTableGlobalConfig.getJavaTypeToDatabaseTypeConverter();
        PgsqlColumnMetadata pgsqlColumnMetadata = new PgsqlColumnMetadata();
        pgsqlColumnMetadata.setName(TableBeanUtils.getRealColumnName(clazz, field));
        pgsqlColumnMetadata.setType(javaTypeToDatabaseTypeConverter.convert(DatabaseDialect.PostgreSQL, clazz, field));
        pgsqlColumnMetadata.setNotNull(TableBeanUtils.isNotNull(field, clazz));
        pgsqlColumnMetadata.setPrimary(TableBeanUtils.isPrimary(field, clazz));
        pgsqlColumnMetadata.setAutoIncrement(TableBeanUtils.isAutoIncrement(field, clazz));
        ColumnDefault columnDefault = TableBeanUtils.getDefaultValue(field);
        if (columnDefault != null) {
            pgsqlColumnMetadata.setDefaultValueType(columnDefault.type());
            String defaultValue = columnDefault.value();
            if(StringUtils.hasText(defaultValue)) {
                DatabaseTypeAndLength type = pgsqlColumnMetadata.getType();
                // 补偿逻辑：字符串类型，前后自动添加'
                if (PgsqlTypeHelper.isCharString(type) && !defaultValue.isEmpty() && !defaultValue.startsWith("'") && !defaultValue.endsWith("'")) {
                    defaultValue = "'" + defaultValue + "'";
                }
                pgsqlColumnMetadata.setDefaultValue(defaultValue);
            }
        }
        pgsqlColumnMetadata.setComment(TableBeanUtils.getComment(field));

        return pgsqlColumnMetadata;
    }
}
