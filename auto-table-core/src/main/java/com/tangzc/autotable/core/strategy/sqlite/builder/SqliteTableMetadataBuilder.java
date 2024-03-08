package com.tangzc.autotable.core.strategy.sqlite.builder;

import com.tangzc.autotable.annotation.TableComment;
import com.tangzc.autotable.core.builder.IndexMetadataBuilder;
import com.tangzc.autotable.core.strategy.ColumnMetadata;
import com.tangzc.autotable.core.strategy.sqlite.data.SqliteTableMetadata;
import com.tangzc.autotable.core.utils.BeanClassUtil;
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
                .map(field -> SqliteColumnMetadataBuilder.build(clazz, field))
                .collect(Collectors.toList());
    }
}