package com.tangzc.autotable.core.builder;

import com.tangzc.autotable.annotation.TableComment;
import com.tangzc.autotable.core.strategy.DefaultTableMetadata;
import com.tangzc.autotable.core.utils.BeanClassUtil;
import com.tangzc.autotable.core.utils.TableBeanUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author don
 */
@Slf4j
public class TableMetadataBuilder {

    public static DefaultTableMetadata build(String databaseDialect, Class<?> clazz) {

        String tableName = TableBeanUtils.getTableName(clazz);

        DefaultTableMetadata pgsqlTableMetadata = new DefaultTableMetadata(clazz, tableName);

        TableComment tableComment = TableBeanUtils.getTableComment(clazz);
        if (tableComment != null) {
            // 获取表注释
            pgsqlTableMetadata.setComment(tableComment.value());
        }

        List<Field> fields = BeanClassUtil.listAllFieldForColumn(clazz);
        pgsqlTableMetadata.setColumnMetadataList(ColumnMetadataBuilder.of(databaseDialect).buildList(clazz, fields));
        pgsqlTableMetadata.setIndexMetadataList(IndexMetadataBuilder.of().buildList(clazz, fields));

        return pgsqlTableMetadata;
    }
}
