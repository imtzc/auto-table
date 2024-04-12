package com.tangzc.autotable.core.builder;

import com.tangzc.autotable.annotation.Index;
import com.tangzc.autotable.annotation.IndexField;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.strategy.IndexMetadata;
import com.tangzc.autotable.core.utils.IndexRepeatChecker;
import com.tangzc.autotable.core.utils.StringUtils;
import com.tangzc.autotable.core.utils.TableBeanUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author don
 */
public class IndexMetadataBuilder {

    public <T extends IndexMetadata> List<T> buildList(Class<?> clazz, List<Field> fields) {

        IndexRepeatChecker indexRepeatChecker = IndexRepeatChecker.of();

        List<IndexMetadata> indexMetadataList = new ArrayList<>(16);

        // 类上的索引注解
        List<IndexMetadata> onClassIndexMetadata = buildFromClass(clazz, indexRepeatChecker);
        indexMetadataList.addAll(onClassIndexMetadata);

        // 字段上的索引注解
        List<IndexMetadata> onFieldIndexMetadata = buildFromField(clazz, fields, indexRepeatChecker);
        indexMetadataList.addAll(onFieldIndexMetadata);

        return (List<T>) indexMetadataList;
    }

    protected List<IndexMetadata> buildFromField(Class<?> clazz, List<Field> fields, IndexRepeatChecker indexRepeatChecker) {
        return fields.stream()
                .filter(field -> TableBeanUtils.isIncludeField(field, clazz))
                .map(field -> buildIndexMetadata(clazz, field))
                .filter(Objects::nonNull)
                .peek(indexMetadata -> indexRepeatChecker.filter(indexMetadata.getName()))
                .collect(Collectors.toList());
    }

    protected List<IndexMetadata> buildFromClass(Class<?> clazz, IndexRepeatChecker indexRepeatChecker) {
        List<TableIndex> tableIndexes = TableBeanUtils.getTableIndexes(clazz);
        return tableIndexes.stream()
                .map(tableIndex -> buildIndexMetadata(clazz, tableIndex))
                .filter(Objects::nonNull)
                .peek(indexMetadata -> indexRepeatChecker.filter(indexMetadata.getName()))
                .collect(Collectors.toList());
    }

    protected IndexMetadata buildIndexMetadata(Class<?> clazz, Field field) {
        // 获取当前字段的@Index注解
        Index index = TableBeanUtils.getIndex(field);
        if (null != index) {
            String realColumnName = TableBeanUtils.getRealColumnName(clazz, field);
            IndexMetadata indexMetadata = newIndexMetadata();
            String indexName = getIndexName(clazz, field, index);
            indexMetadata.setName(indexName);
            indexMetadata.setType(index.type());
            indexMetadata.setComment(index.comment());
            indexMetadata.getColumns().add(IndexMetadata.IndexColumnParam.newInstance(realColumnName, null));
            return indexMetadata;
        }
        return null;
    }

    protected String getIndexName(Class<?> clazz, Field field, Index index) {
        String indexName = index.name();
        if (StringUtils.noText(indexName)) {
            indexName = getDefaultIndexName(clazz, field);
        }
        String indexPrefix = AutoTableGlobalConfig.getAutoTableProperties().getIndexPrefix();
        return indexPrefix + indexName;
    }

    protected String getDefaultIndexName(Class<?> clazz, Field field) {
        return TableBeanUtils.getRealColumnName(clazz, field);
    }

    protected IndexMetadata buildIndexMetadata(Class<?> clazz, TableIndex tableIndex) {

        // 获取当前字段的@Index注解
        if (null != tableIndex) {

            List<IndexMetadata.IndexColumnParam> columnParams = getColumnParams(clazz, tableIndex);

            IndexMetadata indexMetadata = newIndexMetadata();
            String indexPrefix = AutoTableGlobalConfig.getAutoTableProperties().getIndexPrefix();
            indexMetadata.setName(indexPrefix + tableIndex.name());
            indexMetadata.setType(tableIndex.type());
            indexMetadata.setComment(tableIndex.comment());
            indexMetadata.setColumns(columnParams);
            return indexMetadata;
        }
        return null;
    }

    protected IndexMetadata newIndexMetadata() {
        return new IndexMetadata();
    }

    protected List<IndexMetadata.IndexColumnParam> getColumnParams(Class<?> clazz, final TableIndex tableIndex) {
        List<IndexMetadata.IndexColumnParam> columnParams = new ArrayList<>();
        // 防止 两种模式设置的字段有冲突
        Set<String> exitsColumns = new HashSet<>();
        // 优先获取 带排序方式的字段
        IndexField[] sortFields = tableIndex.indexFields();
        if (sortFields.length > 0) {
            columnParams.addAll(
                    Arrays.stream(sortFields)
                            .map(sortField -> {
                                String realColumnName = TableBeanUtils.getRealColumnName(clazz, sortField.field());
                                // 重复字段，自动排除忽略掉
                                if (exitsColumns.contains(realColumnName)) {
                                    return null;
                                }
                                exitsColumns.add(realColumnName);
                                return IndexMetadata.IndexColumnParam.newInstance(realColumnName, sortField.sort());
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList())
            );
        }
        // 其次获取 简单模式的字段，如果重复了，跳过，以带排序方式的为准
        String[] fields = tableIndex.fields();
        if (fields.length > 0) {
            columnParams.addAll(
                    Arrays.stream(fields)
                            .map(field -> {
                                String realColumnName = TableBeanUtils.getRealColumnName(clazz, field);
                                // 重复字段，自动排除忽略掉
                                if (exitsColumns.contains(realColumnName)) {
                                    return null;
                                }
                                exitsColumns.add(realColumnName);
                                return IndexMetadata.IndexColumnParam.newInstance(realColumnName, null);
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList())
            );
        }

        return columnParams;
    }
}
