package com.tangzc.autotable.core.strategy.sqlite.builder;

import com.tangzc.autotable.core.builder.TableMetadataBuilder;
import com.tangzc.autotable.core.constants.DatabaseDialect;
import com.tangzc.autotable.core.converter.DatabaseTypeAndLength;
import com.tangzc.autotable.core.strategy.ColumnMetadata;
import com.tangzc.autotable.core.strategy.DefaultTableMetadata;
import com.tangzc.autotable.core.strategy.sqlite.SqliteTypeHelper;
import com.tangzc.autotable.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author don
 */
@Slf4j
public class SqliteTableMetadataBuilder {

    public static DefaultTableMetadata build(Class<?> clazz) {

        // 获取默认表元数据
        DefaultTableMetadata defaultTableMetadata = TableMetadataBuilder.build(DatabaseDialect.PostgreSQL, clazz);

        defaultTableMetadata.getColumnMetadataList().forEach(columnMetadata -> {
            // 修正默认值
            fixDefaultValue(columnMetadata);
            // 修正类型和长度
            fixTypeAndLength(columnMetadata.getType());
        });

        return defaultTableMetadata;
    }

    private static void fixDefaultValue(ColumnMetadata columnMetadata) {
        String defaultValue = columnMetadata.getDefaultValue();
        if (StringUtils.hasText(defaultValue)) {
            // 补偿逻辑：类型为Boolean的时候(实际数据库为bit数字类型)，兼容 true、false
            boolean isBooleanType = SqliteTypeHelper.isInteger(columnMetadata.getType()) &&
                    ("true".equalsIgnoreCase(defaultValue) || "false".equalsIgnoreCase(defaultValue));
            if (isBooleanType) {
                if (Boolean.parseBoolean(defaultValue)) {
                    defaultValue = "1";
                } else {
                    defaultValue = "0";
                }
            }
            // 补偿逻辑：字符串类型，前后自动添加'
            if (SqliteTypeHelper.isText(columnMetadata.getType()) && !defaultValue.isEmpty() && !defaultValue.startsWith("'") && !defaultValue.endsWith("'")) {
                defaultValue = "'" + defaultValue + "'";
            }
            columnMetadata.setDefaultValue(defaultValue);
        }
    }

    private static void fixTypeAndLength(DatabaseTypeAndLength typeAndLength) {
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
    }
}