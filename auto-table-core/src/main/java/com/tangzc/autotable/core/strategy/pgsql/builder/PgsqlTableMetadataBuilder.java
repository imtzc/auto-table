package com.tangzc.autotable.core.strategy.pgsql.builder;

import com.tangzc.autotable.annotation.TableComment;
import com.tangzc.autotable.core.builder.ColumnMetadataBuilder;
import com.tangzc.autotable.core.builder.IndexMetadataBuilder;
import com.tangzc.autotable.core.constants.DatabaseDialect;
import com.tangzc.autotable.core.converter.DatabaseTypeAndLength;
import com.tangzc.autotable.core.strategy.ColumnMetadata;
import com.tangzc.autotable.core.strategy.pgsql.data.PgsqlTableMetadata;
import com.tangzc.autotable.core.strategy.pgsql.data.PgsqlTypeHelper;
import com.tangzc.autotable.core.utils.BeanClassUtil;
import com.tangzc.autotable.core.utils.StringUtils;
import com.tangzc.autotable.core.utils.TableBeanUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author don
 */
@Slf4j
public class PgsqlTableMetadataBuilder {

    public static PgsqlTableMetadata build(Class<?> clazz) {

        String tableName = TableBeanUtils.getTableName(clazz);

        PgsqlTableMetadata pgsqlTableMetadata = new PgsqlTableMetadata(tableName);

        TableComment tableComment = TableBeanUtils.getTableComment(clazz);
        if (tableComment != null) {
            // 获取表注释
            pgsqlTableMetadata.setComment(tableComment.value());
        }

        List<Field> fields = BeanClassUtil.getAllDeclaredFieldsExcludeStatic(clazz);
        pgsqlTableMetadata.setColumnMetadataList(getColumnList(clazz, fields));
        pgsqlTableMetadata.setIndexMetadataList(IndexMetadataBuilder.buildList(clazz, fields));

        return pgsqlTableMetadata;
    }

    public static List<ColumnMetadata> getColumnList(Class<?> clazz, List<Field> fields) {
        return fields.stream()
                .filter(field -> TableBeanUtils.isIncludeField(field, clazz))
                .map(field -> ColumnMetadataBuilder.of(DatabaseDialect.PostgreSQL, new ColumnMetadata())
                        .buildFromAnnotation(clazz, field, PgsqlTableMetadataBuilder::fixDefaultValue)
                )
                .collect(Collectors.toList());
    }

    private static void fixDefaultValue(ColumnMetadata columnMetadata) {

        String defaultValue = columnMetadata.getDefaultValue();
        if (StringUtils.hasText(defaultValue)) {
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
