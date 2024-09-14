package com.tangzc.autotable.core.strategy.pgsql;

import com.tangzc.autotable.annotation.enums.DefaultValueEnum;
import com.tangzc.autotable.annotation.enums.IndexSortTypeEnum;
import com.tangzc.autotable.annotation.enums.IndexTypeEnum;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.constants.DatabaseDialect;
import com.tangzc.autotable.core.converter.DefaultTypeEnumInterface;
import com.tangzc.autotable.core.strategy.ColumnMetadata;
import com.tangzc.autotable.core.strategy.DefaultTableMetadata;
import com.tangzc.autotable.core.strategy.IStrategy;
import com.tangzc.autotable.core.strategy.IndexMetadata;
import com.tangzc.autotable.core.strategy.pgsql.builder.CreateTableSqlBuilder;
import com.tangzc.autotable.core.strategy.pgsql.builder.ModifyTableSqlBuilder;
import com.tangzc.autotable.core.strategy.pgsql.builder.PgsqlTableMetadataBuilder;
import com.tangzc.autotable.core.strategy.pgsql.data.PgsqlCompareTableInfo;
import com.tangzc.autotable.core.strategy.pgsql.data.PgsqlDefaultTypeEnum;
import com.tangzc.autotable.core.strategy.pgsql.data.dbdata.PgsqlDbColumn;
import com.tangzc.autotable.core.strategy.pgsql.data.dbdata.PgsqlDbIndex;
import com.tangzc.autotable.core.strategy.pgsql.data.dbdata.PgsqlDbPrimary;
import com.tangzc.autotable.core.strategy.pgsql.mapper.PgsqlTablesMapper;
import com.tangzc.autotable.core.utils.StringUtils;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author don
 */
public class PgsqlStrategy implements IStrategy<DefaultTableMetadata, PgsqlCompareTableInfo, PgsqlTablesMapper> {

    @Override
    public String databaseDialect() {
        return DatabaseDialect.PostgreSQL;
    }

    @Override
    public Map<Class<?>, DefaultTypeEnumInterface> typeMapping() {
        return new HashMap<Class<?>, DefaultTypeEnumInterface>(32) {{
            put(String.class, PgsqlDefaultTypeEnum.VARCHAR);
            put(Character.class, PgsqlDefaultTypeEnum.CHAR);
            put(char.class, PgsqlDefaultTypeEnum.CHAR);

            put(BigInteger.class, PgsqlDefaultTypeEnum.INT8);
            put(Long.class, PgsqlDefaultTypeEnum.INT8);
            put(long.class, PgsqlDefaultTypeEnum.INT8);

            put(Integer.class, PgsqlDefaultTypeEnum.INT4);
            put(int.class, PgsqlDefaultTypeEnum.INT4);

            put(Boolean.class, PgsqlDefaultTypeEnum.BOOL);
            put(boolean.class, PgsqlDefaultTypeEnum.BOOL);

            put(Float.class, PgsqlDefaultTypeEnum.FLOAT4);
            put(float.class, PgsqlDefaultTypeEnum.FLOAT4);
            put(Double.class, PgsqlDefaultTypeEnum.FLOAT8);
            put(double.class, PgsqlDefaultTypeEnum.FLOAT8);
            put(BigDecimal.class, PgsqlDefaultTypeEnum.NUMERIC);

            put(Date.class, PgsqlDefaultTypeEnum.TIMESTAMP);
            put(java.sql.Date.class, PgsqlDefaultTypeEnum.TIMESTAMP);
            put(java.sql.Timestamp.class, PgsqlDefaultTypeEnum.TIMESTAMP);
            put(java.sql.Time.class, PgsqlDefaultTypeEnum.TIME);
            put(LocalDateTime.class, PgsqlDefaultTypeEnum.TIMESTAMP);
            put(LocalDate.class, PgsqlDefaultTypeEnum.DATE);
            put(LocalTime.class, PgsqlDefaultTypeEnum.TIME);

            put(Short.class, PgsqlDefaultTypeEnum.INT2);
            put(short.class, PgsqlDefaultTypeEnum.INT2);
        }};
    }

    @Override
    public String dropTable(String schema, String tableName) {
        return String.format("DROP TABLE IF EXISTS %s", withSchemaName(schema, tableName));
    }

    @Override
    public @NonNull DefaultTableMetadata analyseClass(Class<?> beanClass) {

        return new PgsqlTableMetadataBuilder().build(beanClass);
    }

    @Override
    public List<String> createTable(DefaultTableMetadata tableMetadata) {
        String sql = CreateTableSqlBuilder.buildSql(tableMetadata);
        return Collections.singletonList(sql);
    }

    @Override
    public @NonNull PgsqlCompareTableInfo compareTable(DefaultTableMetadata tableMetadata) {

        String tableName = tableMetadata.getTableName();
        String schema = tableMetadata.getSchema();

        PgsqlCompareTableInfo pgsqlCompareTableInfo = new PgsqlCompareTableInfo(tableName, schema);

        // 比较表信息
        compareTableInfo(tableMetadata, pgsqlCompareTableInfo);

        // 比较字段信息
        compareColumnInfo(tableMetadata, pgsqlCompareTableInfo);

        // 比较索引信息
        compareIndexInfo(tableMetadata, pgsqlCompareTableInfo);

        return pgsqlCompareTableInfo;
    }

    private void compareIndexInfo(DefaultTableMetadata tableMetadata, PgsqlCompareTableInfo pgsqlCompareTableInfo) {

        String tableName = tableMetadata.getTableName();
        String schema = tableMetadata.getSchema();

        List<PgsqlDbIndex> pgsqlDbIndices = executeReturn(pgsqlTablesMapper -> pgsqlTablesMapper.selectTableIndexesDetail(schema, tableName));
        Map<String, PgsqlDbIndex> pgsqlDbIndexMap = pgsqlDbIndices.stream()
                .collect(Collectors.toMap(PgsqlDbIndex::getIndexName, Function.identity()));

        List<IndexMetadata> indexMetadataList = tableMetadata.getIndexMetadataList();
        for (IndexMetadata indexMetadata : indexMetadataList) {
            String indexName = indexMetadata.getName();
            String comment = indexMetadata.getComment();
            // 尝试从索引标记集合中删除索引
            PgsqlDbIndex dbIndex = pgsqlDbIndexMap.remove(indexName);
            // 删除失败，表示是新增的索引
            boolean isNewIndex = dbIndex == null;
            if (isNewIndex) {
                // 标记注释
                if (StringUtils.hasText(comment)) {
                    pgsqlCompareTableInfo.addIndexComment(indexMetadata.getName(), comment);
                }
                // 标记索引信息
                pgsqlCompareTableInfo.addNewIndex(indexMetadata);
                continue;
            }
            // 修改索引注释
            boolean anyOneIsValid = StringUtils.hasText(dbIndex.getDescription()) || StringUtils.hasText(comment);
            if (anyOneIsValid && !Objects.equals(dbIndex.getDescription(), comment)) {
                pgsqlCompareTableInfo.addIndexComment(indexName, comment);
            }

            // 获取索引定义语句，进行比较  CREATE UNIQUE INDEX mpe_idx_phone_index ON "public".my_pgsql_table USING btree (phone DESC)
            String indexdef = dbIndex.getIndexdef().replace("\"", "");
            boolean isUniqueIndex = indexMetadata.getType() == IndexTypeEnum.UNIQUE;
            // 索引改变
            String indexColumnParams = indexMetadata.getColumns().stream().map(col -> col.getColumn() + (col.getSort() == IndexSortTypeEnum.DESC ? " DESC" : "")).collect(Collectors.joining(", "));
            if (!indexdef.matches("^CREATE " + (isUniqueIndex ? "UNIQUE INDEX" : "INDEX") + " " + indexName + " ON " + PgsqlStrategy.withSchemaName(schema, tableName) + " USING btree \\(" + indexColumnParams + "\\)$")) {
                pgsqlCompareTableInfo.addModifyIndex(indexMetadata);
            }
        }

        // 需要删除的索引
        Set<String> needRemoveIndexes = pgsqlDbIndexMap.keySet();
        if (!needRemoveIndexes.isEmpty()) {
            // 根据配置，决定是否删除库上的多余索引
            if (AutoTableGlobalConfig.getAutoTableProperties().getAutoDropIndex()) {
                pgsqlCompareTableInfo.addDropIndexes(needRemoveIndexes);
            }
        }
    }

    private void compareColumnInfo(DefaultTableMetadata tableMetadata, PgsqlCompareTableInfo pgsqlCompareTableInfo) {

        String tableName = tableMetadata.getTableName();
        String schema = tableMetadata.getSchema();
        // 数据库字段元信息
        List<PgsqlDbColumn> pgsqlDbColumns = executeReturn(pgsqlTablesMapper -> pgsqlTablesMapper.selectTableFieldDetail(schema, tableName));
        Map<String, PgsqlDbColumn> pgsqlFieldDetailMap = pgsqlDbColumns.stream().collect(Collectors.toMap(PgsqlDbColumn::getColumnName, Function.identity()));
        // 当前字段信息
        List<ColumnMetadata> columnMetadataList = tableMetadata.getColumnMetadataList();

        for (ColumnMetadata columnMetadata : columnMetadataList) {
            String columnName = columnMetadata.getName();
            PgsqlDbColumn pgsqlDbColumn = pgsqlFieldDetailMap.remove(columnName);
            // 新增字段
            if (pgsqlDbColumn == null) {
                // 标记注释
                pgsqlCompareTableInfo.addColumnComment(columnMetadata.getName(), columnMetadata.getComment());
                // 标记字段信息
                pgsqlCompareTableInfo.addNewColumn(columnMetadata);
                continue;
            }
            /* 修改的字段 */
            // 修改了字段注释
            if (!Objects.equals(pgsqlDbColumn.getDescription(), columnMetadata.getComment())) {
                pgsqlCompareTableInfo.addColumnComment(columnName, columnMetadata.getComment());
            }
            // 主键忽略判断，单独处理
            if (!columnMetadata.isPrimary()) {
                // 字段类型不同
                boolean isTypeDiff = isTypeDiff(columnMetadata, pgsqlDbColumn);
                // 非null不同
                boolean isNotnullDiff = columnMetadata.isNotNull() != Objects.equals(pgsqlDbColumn.getIsNullable(), "NO");
                // 默认值不同
                boolean isDefaultDiff = isDefaultDiff(columnMetadata, pgsqlDbColumn);
                if (isTypeDiff || isNotnullDiff || isDefaultDiff) {
                    pgsqlCompareTableInfo.addModifyColumn(columnMetadata);
                }
            }
        }
        // 需要删除的字段
        Set<String> needRemoveColumns = pgsqlFieldDetailMap.keySet();
        if (!needRemoveColumns.isEmpty()) {
            // 根据配置，决定是否删除库上的多余字段
            if (AutoTableGlobalConfig.getAutoTableProperties().getAutoDropColumn()) {
                pgsqlCompareTableInfo.addDropColumns(needRemoveColumns);
            }
        }

        /* 处理主键 */
        // 获取所有主键
        List<ColumnMetadata> primaryColumnList = columnMetadataList.stream().filter(ColumnMetadata::isPrimary).collect(Collectors.toList());
        Set<String> newPrimaryColumns = primaryColumnList.stream().map(ColumnMetadata::getName).collect(Collectors.toSet());
        // 查询数据库主键信息
        PgsqlDbPrimary pgsqlDbPrimary = executeReturn(pgsqlTablesMapper -> pgsqlTablesMapper.selectPrimaryKeyName(schema, tableName));
        HashSet<String> dbPrimaryColumns = pgsqlDbPrimary == null ? new HashSet<>() : new HashSet<>(Arrays.asList(pgsqlDbPrimary.getColumns().split(",")));

        boolean primaryChange = !dbPrimaryColumns.equals(newPrimaryColumns);
        if (primaryChange) {
            // 标记待删除的主键
            pgsqlCompareTableInfo.setDropPrimaryKeyName(pgsqlDbPrimary.getPrimaryName());
        }
        boolean newPrimary = !primaryColumnList.isEmpty() && pgsqlDbPrimary == null;
        if (newPrimary || primaryChange) {
            // 标记新创建的主键
            pgsqlCompareTableInfo.addNewPrimary(primaryColumnList);
        }
    }

    private boolean isTypeDiff(ColumnMetadata columnMetadata, PgsqlDbColumn pgsqlDbColumn) {
        String dataTypeFormat = pgsqlDbColumn.getDataTypeFormat();
        String fullType = columnMetadata.getType().getDefaultFullType().toLowerCase();
        // 数字类型的，默认没有长度，但是数据库查询出来的有长度。 "int4(32)".startWith("int4")
        if (dataTypeFormat.startsWith("int")) {
            return !dataTypeFormat.startsWith(fullType);
        }
        return !Objects.equals(fullType, dataTypeFormat);
    }

    private boolean isDefaultDiff(ColumnMetadata columnMetadata, PgsqlDbColumn pgsqlDbColumn) {

        String columnDefault = pgsqlDbColumn.getColumnDefault();
        // 纠正default值，去掉类型转换
        if (columnDefault != null) {
            int castChart = columnDefault.indexOf("::");
            if (castChart > 0) {
                columnDefault = columnDefault.substring(0, castChart);
            }
        }

        DefaultValueEnum defaultValueType = columnMetadata.getDefaultValueType();

        if (DefaultValueEnum.isValid(defaultValueType)) {
            if (defaultValueType == DefaultValueEnum.EMPTY_STRING) {
                return !"''".equals(columnDefault);
            }
            if (defaultValueType == DefaultValueEnum.NULL) {
                return columnDefault != null && !"NULL".equalsIgnoreCase(columnDefault);
            }
        } else {
            String defaultValue = columnMetadata.getDefaultValue();
            return !Objects.equals(defaultValue, columnDefault);
        }
        return false;
    }

    private void compareTableInfo(DefaultTableMetadata tableMetadata, PgsqlCompareTableInfo pgsqlCompareTableInfo) {

        String tableName = tableMetadata.getTableName();
        String schema = tableMetadata.getSchema();

        String tableDescription = executeReturn(pgsqlTablesMapper -> pgsqlTablesMapper.selectTableDescription(schema, tableName));
        if (!Objects.equals(tableDescription, tableMetadata.getComment())) {
            pgsqlCompareTableInfo.setComment(tableMetadata.getComment());
        }
    }

    @Override
    public List<String> modifyTable(PgsqlCompareTableInfo pgsqlCompareTableInfo) {
        String sql = ModifyTableSqlBuilder.buildSql(pgsqlCompareTableInfo);
        return Collections.singletonList(sql);
    }

    public static String withSchemaName(String schema, String name) {
        return StringUtils.hasText(schema) ? (schema + "." + name) : name;
    }
}
