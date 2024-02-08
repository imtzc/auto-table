package com.tangzc.autotable.core.strategy.sqlite.builder;

import com.tangzc.autotable.annotation.TableComment;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.strategy.sqlite.data.SqliteColumnMetadata;
import com.tangzc.autotable.core.strategy.sqlite.data.SqliteIndexMetadata;
import com.tangzc.autotable.core.strategy.sqlite.data.SqliteTableMetadata;
import com.tangzc.autotable.core.utils.BeanClassUtil;
import com.tangzc.autotable.core.utils.IndexRepeatChecker;
import com.tangzc.autotable.core.utils.TableBeanUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
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
        mysqlTableMetadata.setIndexMetadataList(getIndexList(clazz, fields));

        return mysqlTableMetadata;
    }

    public static List<SqliteColumnMetadata> getColumnList(Class<?> clazz, List<Field> fields) {
        return fields.stream()
                .filter(field -> TableBeanUtils.isIncludeField(field, clazz))
                .map(field -> SqliteColumnMetadataBuilder.build(clazz, field))
                .collect(Collectors.toList());
    }

    public static List<SqliteIndexMetadata> getIndexList(Class<?> clazz, List<Field> fields) {

        IndexRepeatChecker indexRepeatChecker = IndexRepeatChecker.of();

        // 类上的索引注解
        List<TableIndex> tableIndexes = TableBeanUtils.getTableIndexes(clazz);
        List<SqliteIndexMetadata> indexMetadataList = tableIndexes.stream()
                .map(tableIndex -> SqliteIndexMetadataBuilder.build(clazz, tableIndex, AutoTableGlobalConfig.getAutoTableProperties().getIndexPrefix()))
                .filter(Objects::nonNull)
                .filter(indexMetadata -> indexRepeatChecker.filter(indexMetadata.getName()))
                .collect(Collectors.toList());

        // 字段上的索引注解
        List<SqliteIndexMetadata> onFieldIndexMetadata = fields.stream()
                .filter(field -> TableBeanUtils.isIncludeField(field, clazz))
                .map(field -> SqliteIndexMetadataBuilder.build(clazz, field, AutoTableGlobalConfig.getAutoTableProperties().getIndexPrefix()))
                .filter(Objects::nonNull)
                .filter(indexMetadata -> indexRepeatChecker.filter(indexMetadata.getName()))
                .collect(Collectors.toList());
        indexMetadataList.addAll(onFieldIndexMetadata);
        return indexMetadataList;
    }
}