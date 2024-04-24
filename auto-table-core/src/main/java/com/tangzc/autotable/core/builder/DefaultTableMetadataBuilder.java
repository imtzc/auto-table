package com.tangzc.autotable.core.builder;

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

        String tableName = getTableName(clazz);
        String tableSchema = getTableSchema(clazz);
        String tableComment = getTableComment(clazz);
        DefaultTableMetadata tableMetadata = new DefaultTableMetadata(clazz, tableName, tableSchema, tableComment);

        List<Field> fields = BeanClassUtil.listAllFieldForColumn(clazz);

        // 填充字段
        fillColumnMetadataList(clazz, tableMetadata, fields);

        // 填充索引
        fillIndexMetadataList(clazz, tableMetadata, fields);

        return tableMetadata;
    }

    protected String getTableComment(Class<?> clazz) {
        return TableBeanUtils.getTableComment(clazz);
    }

    protected String getTableSchema(Class<?> clazz) {
        return TableBeanUtils.getTableSchema(clazz);
    }

    protected String getTableName(Class<?> clazz) {
        return TableBeanUtils.getTableName(clazz);
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
