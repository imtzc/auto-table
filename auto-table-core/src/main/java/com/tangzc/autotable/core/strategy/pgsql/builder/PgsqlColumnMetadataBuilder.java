package com.tangzc.autotable.core.strategy.pgsql.builder;

import com.tangzc.autotable.core.builder.ColumnMetadataBuilder;
import com.tangzc.autotable.core.constants.DatabaseDialect;
import com.tangzc.autotable.core.converter.DatabaseTypeAndLength;
import com.tangzc.autotable.core.strategy.ColumnMetadata;
import com.tangzc.autotable.core.strategy.pgsql.data.PgsqlTypeHelper;
import com.tangzc.autotable.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

/**
 * 用于存放创建表的字段信息
 *
 * @author don
 */
@Slf4j
public class PgsqlColumnMetadataBuilder {

    public static ColumnMetadata build(Class<?> clazz, Field field) {

        ColumnMetadata columnMetadata = ColumnMetadataBuilder.of(DatabaseDialect.PostgreSQL, new ColumnMetadata())
                .buildFromAnnotation(clazz, field);

        // 修正默认值
        fixDefaultValue(columnMetadata);

        return columnMetadata;
    }

    private static void fixDefaultValue(ColumnMetadata columnMetadata) {

        String defaultValue = columnMetadata.getDefaultValue();
        if(StringUtils.hasText(defaultValue)) {
            DatabaseTypeAndLength type = columnMetadata.getType();
            // 布尔值，自动转化
            if (PgsqlTypeHelper.isBoolean(type)) {
                if ("1".equals(defaultValue)) {
                    defaultValue = "true";
                } else if ("0".equals(defaultValue)) {
                    defaultValue = "false";
                }
            }
            // 兼容逻辑：如果是字符串的类型，自动包一层''（如果没有的话）
            if (PgsqlTypeHelper.isCharString(type) && !defaultValue.startsWith("'") && !defaultValue.endsWith("'")) {
                defaultValue = "'" + defaultValue + "'";
            }
            // 兼容逻辑：如果是日期，且非函数，自动包一层''（如果没有的话）
            if (PgsqlTypeHelper.isTime(type) && defaultValue.matches("(\\d+.?)+") && !defaultValue.startsWith("'") && !defaultValue.endsWith("'")) {
                defaultValue = "'" + defaultValue + "'";
            }
            columnMetadata.setDefaultValue(defaultValue);
        }
    }
}
