package com.tangzc.autotable.core.builder;

import com.tangzc.autotable.annotation.TableComment;
import com.tangzc.autotable.core.strategy.ColumnMetadata;
import com.tangzc.autotable.core.strategy.DefaultTableMetadata;
import com.tangzc.autotable.core.strategy.IndexMetadata;
import com.tangzc.autotable.core.utils.BeanClassUtil;
import com.tangzc.autotable.core.utils.TableBeanUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author don
 */
@Slf4j
public class DefaultTableMetadataBuilder {

    protected final ColumnMetadataBuilder columnMetadataBuilder;
    protected final IndexMetadataBuilder indexMetadataBuilder;

    public DefaultTableMetadataBuilder(ColumnMetadataBuilder columnMetadataBuilder, IndexMetadataBuilder indexMetadataBuilder) {
        this.columnMetadataBuilder = columnMetadataBuilder;
        this.indexMetadataBuilder = indexMetadataBuilder;
    }

    public DefaultTableMetadata build(Class<?> clazz) {

        String tableName = TableBeanUtils.getTableName(clazz);

        DefaultTableMetadata tableMetadata = new DefaultTableMetadata(clazz, tableName);

        TableComment tableComment = TableBeanUtils.getTableComment(clazz);
        if (tableComment != null) {
            // 获取表注释
            tableMetadata.setComment(tableComment.value());
        }

        List<Field> fields = BeanClassUtil.listAllFieldForColumn(clazz);

        // 填充字段
        fillColumnMetadataList(clazz, tableMetadata, fields);

        // 填充索引
        fillIndexMetadataList(clazz, tableMetadata, fields);

        return tableMetadata;
    }

    protected void fillIndexMetadataList(Class<?> clazz, DefaultTableMetadata tableMetadata, List<Field> fields) {
        List<IndexMetadata> indexMetadataList = indexMetadataBuilder.buildList(clazz, fields);
        tableMetadata.setIndexMetadataList(indexMetadataList);
    }

    protected void fillColumnMetadataList(Class<?> clazz, DefaultTableMetadata tableMetadata, List<Field> fields) {
        List<ColumnMetadata> columnMetadataList = columnMetadataBuilder.buildList(clazz, fields);
        tableMetadata.setColumnMetadataList(columnMetadataList);
    }
}
