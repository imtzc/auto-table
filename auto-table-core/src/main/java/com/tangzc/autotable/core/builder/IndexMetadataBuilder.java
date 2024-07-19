package com.tangzc.autotable.core.builder;

import com.tangzc.autotable.annotation.Index;
import com.tangzc.autotable.annotation.IndexField;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.strategy.IndexMetadata;
import com.tangzc.autotable.core.utils.IndexRepeatChecker;
import com.tangzc.autotable.core.utils.StringUtils;
import com.tangzc.autotable.core.utils.TableBeanUtils;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    protected String getIndexName(Class<?> clazz, TableIndex tableIndex) {
        String indexPrefix = AutoTableGlobalConfig.getAutoTableProperties().getIndexPrefix();

        // 手动指定了索引名
        String indexName = tableIndex.name();
        if (StringUtils.hasText(indexName)) {
            return indexPrefix + indexName;
        }

        String filedNames = Stream.concat(Arrays.stream(tableIndex.indexFields()).map(IndexField::field), Arrays.stream(tableIndex.fields()))
                .map(fieldName -> TableBeanUtils.getRealColumnName(clazz, fieldName))
                .collect(Collectors.joining("_"));
        String tableName = TableBeanUtils.getTableName(clazz);
        return encryptIndexName(indexPrefix, tableName, filedNames);
    }

    protected String getIndexName(Class<?> clazz, Field field, Index index) {
        String indexPrefix = AutoTableGlobalConfig.getAutoTableProperties().getIndexPrefix();

        // 手动指定了索引名
        String indexName = index.name();
        if (StringUtils.hasText(indexName)) {
            return indexPrefix + indexName;
        }

        String realColumnName = getDefaultIndexName(clazz, field);
        String tableName = TableBeanUtils.getTableName(clazz);
        return encryptIndexName(indexPrefix, tableName, realColumnName);
    }

    protected String getDefaultIndexName(Class<?> clazz, Field field) {
        return TableBeanUtils.getRealColumnName(clazz, field);
    }

    protected String encryptIndexName(String prefix, String tableNamePart, String filedNamePart) {
        String indexName = prefix + tableNamePart + "_" + filedNamePart;
        int maxLength = 63;
        if (indexName.length() > maxLength) {
            String md5 = generateMD5(indexName);
            if (prefix.length() + md5.length() > maxLength) {
                throw new RuntimeException("索引名前缀[" + prefix + "]超长，无法生成有效索引名称，请手动指定索引名称");
            }
            // 截取前半部分长度的字符，空余足够的位置，给“_”和MD5值
            String onePart = indexName.substring(0, maxLength - md5.length());
            return onePart + md5;
        }
        return indexName;
    }

    @SneakyThrows
    private String generateMD5(String text) {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashBytes = md.digest(text.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    protected IndexMetadata buildIndexMetadata(Class<?> clazz, TableIndex tableIndex) {

        // 获取当前字段的@Index注解
        if (null != tableIndex && (tableIndex.fields().length > 0 || tableIndex.indexFields().length > 0)) {

            List<IndexMetadata.IndexColumnParam> columnParams = getColumnParams(clazz, tableIndex);

            IndexMetadata indexMetadata = newIndexMetadata();
            indexMetadata.setName(getIndexName(clazz, tableIndex));
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
