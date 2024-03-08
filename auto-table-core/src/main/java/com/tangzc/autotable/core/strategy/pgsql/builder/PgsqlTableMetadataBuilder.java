package com.tangzc.autotable.core.strategy.pgsql.builder;

import com.tangzc.autotable.annotation.TableComment;
import com.tangzc.autotable.core.builder.IndexMetadataBuilder;
import com.tangzc.autotable.core.strategy.ColumnMetadata;
import com.tangzc.autotable.core.strategy.pgsql.data.PgsqlTableMetadata;
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
        pgsqlTableMetadata.setIndexMetadataList(IndexMetadataBuilder.buildList(clazz, fields));

        return pgsqlTableMetadata;
    }

    public static List<ColumnMetadata> getColumnList(Class<?> clazz, List<Field> fields) {
        return fields.stream()
                .filter(field -> TableBeanUtils.isIncludeField(field, clazz))
                .map(field -> PgsqlColumnMetadataBuilder.build(clazz, field))
                .collect(Collectors.toList());
    }
}
