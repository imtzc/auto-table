package com.tangzc.autotable.core.strategy.mysql.builder;

import com.tangzc.autotable.annotation.Index;
import com.tangzc.autotable.annotation.IndexField;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.autotable.core.strategy.mysql.data.MysqlIndexMetadata;
import com.tangzc.autotable.core.utils.StringUtils;
import com.tangzc.autotable.core.utils.TableBeanUtils;
import lombok.Data;
import lombok.experimental.Accessors;

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
public class MysqlIndexMetadataBuilder {

    public static MysqlIndexMetadata build(Class<?> clazz, Field field, String indexPrefix) {
        // 获取当前字段的@Index注解
        Index index = TableBeanUtils.getIndex(field);
        if (null != index) {
            String realColumnName = TableBeanUtils.getRealColumnName(clazz, field);
            MysqlIndexMetadata mysqlIndexMetadata = new MysqlIndexMetadata();
            String indexName = index.name();
            if (!StringUtils.hasText(indexName)) {
                indexName = TableBeanUtils.getRealColumnName(clazz, field);
            }
            mysqlIndexMetadata.setName(indexPrefix + indexName);
            mysqlIndexMetadata.setType(index.type());
            mysqlIndexMetadata.setComment(index.comment());
            mysqlIndexMetadata.getColumns().add(MysqlIndexMetadata.IndexColumnParam.newInstance(realColumnName, null));
            return mysqlIndexMetadata;
        }
        return null;
    }

    public static MysqlIndexMetadata build(Class<?> clazz, TableIndex tableIndex, String indexPrefix) {

        // 获取当前字段的@Index注解
        if (null != tableIndex) {

            List<MysqlIndexMetadata.IndexColumnParam> columnParams = getColumnParams(clazz, tableIndex);

            MysqlIndexMetadata mysqlIndexMetadata = new MysqlIndexMetadata();
            mysqlIndexMetadata.setName(indexPrefix + tableIndex.name());
            mysqlIndexMetadata.setType(tableIndex.type());
            mysqlIndexMetadata.setComment(tableIndex.comment());
            mysqlIndexMetadata.setColumns(columnParams);
            return mysqlIndexMetadata;
        }
        return null;
    }

    private static List<MysqlIndexMetadata.IndexColumnParam> getColumnParams(Class<?> clazz, final TableIndex tableIndex) {
        List<MysqlIndexMetadata.IndexColumnParam> columnParams = new ArrayList<>();
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
                                return MysqlIndexMetadata.IndexColumnParam.newInstance(realColumnName, sortField.sort());
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
                                return MysqlIndexMetadata.IndexColumnParam.newInstance(realColumnName, null);
                            })
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList())
            );
        }

        return columnParams;
    }
}
