package com.tangzc.autotable.core.strategy.sqlite.builder;

import com.tangzc.autotable.annotation.TableComment;
import com.tangzc.autotable.core.builder.ColumnMetadataBuilder;
import com.tangzc.autotable.core.builder.IndexMetadataBuilder;
import com.tangzc.autotable.core.constants.DatabaseDialect;
import com.tangzc.autotable.core.converter.DatabaseTypeAndLength;
import com.tangzc.autotable.core.strategy.ColumnMetadata;
import com.tangzc.autotable.core.strategy.sqlite.SqliteTypeHelper;
import com.tangzc.autotable.core.strategy.sqlite.data.SqliteTableMetadata;
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
public class SqliteTableMetadataBuilder {

    public static SqliteTableMetadata build(Class<?> clazz) {

        String tableName = TableBeanUtils.getTableName(clazz);

        SqliteTableMetadata mysqlTableMetadata = new SqliteTableMetadata(tableName);

        TableComment tableComment = TableBeanUtils.getTableComment(clazz);
        assert tableComment != null;
        // 获取表注释
        mysqlTableMetadata.setComment(tableComment.value());

        List<Field> fields = BeanClassUtil.getAllDeclaredFieldsExcludeStatic(clazz);
        mysqlTableMetadata.setColumnMetadataList(getColumnList(clazz, fields));
        mysqlTableMetadata.setIndexMetadataList(IndexMetadataBuilder.buildList(clazz, fields));

        return mysqlTableMetadata;
    }

    public static List<ColumnMetadata> getColumnList(Class<?> clazz, List<Field> fields) {
        return fields.stream()
                .filter(field -> TableBeanUtils.isIncludeField(field, clazz))
                .map(field -> ColumnMetadataBuilder.of(DatabaseDialect.SQLite, new ColumnMetadata())
                        .buildFromAnnotation(clazz, field, columnMetadata -> {
                            // 修正类型和长度
                            fixTypeAndLength(columnMetadata.getType());

                            // 修正默认值
                            fixDefaultValue(field, columnMetadata);
                        }))
                .collect(Collectors.toList());
    }

    private static void fixDefaultValue(Field field, ColumnMetadata columnMetadata) {
        String defaultValue = columnMetadata.getDefaultValue();
        if (StringUtils.hasText(defaultValue)) {
            Class<?> fieldType = field.getType();
            // 补偿逻辑：类型为Boolean的时候(实际数据库为bit数字类型)，兼容 true、false
            boolean isBooleanType = (fieldType == Boolean.class || fieldType == boolean.class) && SqliteTypeHelper.isInteger(columnMetadata.getType());
            if (isBooleanType && !"1".equals(defaultValue) && !"0".equals(defaultValue)) {
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