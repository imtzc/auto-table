package com.tangzc.autotable.core.strategy.pgsql.builder;

import com.tangzc.autotable.annotation.TableComment;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.strategy.pgsql.data.PgsqlColumnMetadata;
import com.tangzc.autotable.core.strategy.pgsql.data.PgsqlIndexMetadata;
import com.tangzc.autotable.core.strategy.pgsql.data.PgsqlTableMetadata;
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
public class PgsqlTableMetadataBuilder {

    public static PgsqlTableMetadata build(Class<?> clazz) {

        String tableName = TableBeanUtils.getTableName(clazz);

        PgsqlTableMetadata pgsqlTableMetadata = new PgsqlTableMetadata(tableName);

        TableComment tableComment = TableBeanUtils.getTableComment(clazz);
        assert tableComment != null;
        // 获取表注释
        pgsqlTableMetadata.setComment(tableComment.value());

        List<Field> fields = BeanClassUtil.getAllDeclaredFieldsExcludeStatic(clazz);
        pgsqlTableMetadata.setColumnMetadataList(getColumnList(clazz, fields));
        pgsqlTableMetadata.setIndexMetadataList(getIndexList(clazz, fields));

        return pgsqlTableMetadata;
    }

    public static List<PgsqlColumnMetadata> getColumnList(Class<?> clazz, List<Field> fields) {
        return fields.stream()
                .filter(field -> TableBeanUtils.isIncludeField(field, clazz))
                .map(field -> PgsqlColumnMetadataBuilder.build(clazz, field))
                .collect(Collectors.toList());
    }

    public static List<PgsqlIndexMetadata> getIndexList(Class<?> clazz, List<Field> fields) {

        IndexRepeatChecker indexRepeatChecker = IndexRepeatChecker.of();

        // 类上的索引注解
        List<TableIndex> tableIndexes = TableBeanUtils.getTableIndexes(clazz);
        List<PgsqlIndexMetadata> indexMetadataList = tableIndexes.stream()
                .map(tableIndex -> PgsqlIndexMetadataBuilder.build(clazz, tableIndex, AutoTableGlobalConfig.getAutoTableProperties().getIndexPrefix()))
                .filter(Objects::nonNull)
                .filter(indexMetadata -> indexRepeatChecker.filter(indexMetadata.getName()))
                .collect(Collectors.toList());

        // 字段上的索引注解
        List<PgsqlIndexMetadata> onFieldIndexMetadata = fields.stream()
                .filter(field -> TableBeanUtils.isIncludeField(field, clazz))
                .map(field -> PgsqlIndexMetadataBuilder.build(clazz, field, AutoTableGlobalConfig.getAutoTableProperties().getIndexPrefix()))
                .filter(Objects::nonNull)
                .filter(indexMetadata -> indexRepeatChecker.filter(indexMetadata.getName()))
                .collect(Collectors.toList());
        indexMetadataList.addAll(onFieldIndexMetadata);
        return indexMetadataList;
    }
}
