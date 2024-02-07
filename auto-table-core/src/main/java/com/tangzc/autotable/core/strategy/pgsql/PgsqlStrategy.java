package com.tangzc.autotable.core.strategy.pgsql;

import com.tangzc.autotable.annotation.enums.DefaultValueEnum;
import com.tangzc.autotable.annotation.enums.IndexSortTypeEnum;
import com.tangzc.autotable.annotation.enums.IndexTypeEnum;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.strategy.IStrategy;
import com.tangzc.autotable.core.strategy.pgsql.builder.CreateTableSqlBuilder;
import com.tangzc.autotable.core.strategy.pgsql.builder.ModifyTableSqlBuilder;
import com.tangzc.autotable.core.strategy.pgsql.builder.PgsqlTableMetadataBuilder;
import com.tangzc.autotable.core.strategy.pgsql.data.PgsqlColumnMetadata;
import com.tangzc.autotable.core.strategy.pgsql.data.PgsqlCompareTableInfo;
import com.tangzc.autotable.core.strategy.pgsql.data.PgsqlIndexMetadata;
import com.tangzc.autotable.core.strategy.pgsql.data.PgsqlTableMetadata;
import com.tangzc.autotable.core.strategy.pgsql.data.dbdata.PgsqlDbColumn;
import com.tangzc.autotable.core.strategy.pgsql.data.dbdata.PgsqlDbIndex;
import com.tangzc.autotable.core.strategy.pgsql.data.dbdata.PgsqlDbPrimary;
import com.tangzc.autotable.core.strategy.pgsql.mapper.PgsqlTablesMapper;
import com.tangzc.autotable.core.utils.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author don
 */
public class PgsqlStrategy implements IStrategy<PgsqlTableMetadata, PgsqlCompareTableInfo, PgsqlTablesMapper> {

    @Override
    public String dbDialect() {
        return "PostgreSQL";
    }

    @Override
    public void dropTable(String tableName) {
        execute(pgsqlTablesMapper -> pgsqlTablesMapper.dropTableByName(tableName));
    }

    @Override
    public boolean checkTableExist(String tableName) {
        return executeReturn(pgsqlTablesMapper -> pgsqlTablesMapper.checkTableExist(tableName) > 0);
    }

    @Override
    public PgsqlTableMetadata analyseClass(Class<?> beanClass) {
        PgsqlTableMetadata pgsqlTableMetadata = PgsqlTableMetadataBuilder.build(beanClass);
        if (pgsqlTableMetadata.getColumnMetadataList().isEmpty()) {
            log.warn("扫描发现{}没有建表字段请检查！", beanClass.getName());
            return null;
        }
        return pgsqlTableMetadata;
    }

    @Override
    public void createTable(PgsqlTableMetadata tableMetadata) {
        String buildSql = CreateTableSqlBuilder.buildSql(tableMetadata);
        log.info("开始创建表：{}", tableMetadata.getTableName());
        log.info("执行SQL：{}", buildSql);
        execute(pgsqlTablesMapper -> pgsqlTablesMapper.executeSql(buildSql));
        log.info("结束创建表：{}", tableMetadata.getTableName());
    }

    @Override
    public PgsqlCompareTableInfo compareTable(PgsqlTableMetadata tableMetadata) {

        String tableName = tableMetadata.getTableName();

        PgsqlCompareTableInfo pgsqlCompareTableInfo = new PgsqlCompareTableInfo(tableName);

        // 比较表信息
        compareTableInfo(tableMetadata, tableName, pgsqlCompareTableInfo);

        // 比较字段信息
        compareColumnInfo(tableMetadata, tableName, pgsqlCompareTableInfo);

        // 比较索引信息
        compareIndexInfo(tableMetadata, tableName, pgsqlCompareTableInfo);

        return pgsqlCompareTableInfo;
    }

    private void compareIndexInfo(PgsqlTableMetadata tableMetadata, String tableName, PgsqlCompareTableInfo pgsqlCompareTableInfo) {
        List<PgsqlDbIndex> pgsqlDbIndices = executeReturn(pgsqlTablesMapper -> pgsqlTablesMapper.selectTableIndexesDetail(tableName));
        Map<String, PgsqlDbIndex> pgsqlDbIndexMap = pgsqlDbIndices.stream()
                // 仅仅处理自定义的索引
                .filter(idx -> idx.getIndexName().startsWith(AutoTableGlobalConfig.getAutoTableProperties().getIndexPrefix()))
                .collect(Collectors.toMap(PgsqlDbIndex::getIndexName, Function.identity()));

        List<PgsqlIndexMetadata> indexMetadataList = tableMetadata.getIndexMetadataList();
        for (PgsqlIndexMetadata pgsqlIndexMetadata : indexMetadataList) {
            String indexName = pgsqlIndexMetadata.getName();
            PgsqlDbIndex dbIndex = pgsqlDbIndexMap.remove(indexName);
            // 新增索引
            String comment = pgsqlIndexMetadata.getComment();
            comment = StringUtils.hasText(comment) ? comment : null;
            if (dbIndex == null) {
                // 标记注释
                if (StringUtils.hasText(comment)) {
                    pgsqlCompareTableInfo.addIndexComment(pgsqlIndexMetadata.getName(), comment);
                }
                // 标记索引信息
                pgsqlCompareTableInfo.addNewIndex(pgsqlIndexMetadata);
                continue;
            }
            // 修改索引注释
            if (!Objects.equals(dbIndex.getDescription(), comment)) {
                pgsqlCompareTableInfo.addIndexComment(indexName, comment);
            }

            // 获取索引定义语句，进行比较  CREATE UNIQUE INDEX mpe_idx_phone_index ON public.my_pgsql_table USING btree (phone DESC)
            String indexdef = dbIndex.getIndexdef().replace("\"", "");
            boolean isUniqueIndex = pgsqlIndexMetadata.getType() == IndexTypeEnum.UNIQUE;
            // 索引改变
            String indexColumnParams = pgsqlIndexMetadata.getColumns().stream().map(col -> col.getColumn() + (col.getSort() == IndexSortTypeEnum.DESC ? " DESC" : "")).collect(Collectors.joining(", "));
            if (!indexdef.matches("^CREATE " + (isUniqueIndex ? "UNIQUE INDEX" : "INDEX") + " " + indexName + " ON public\\." + tableName + " USING btree \\(" + indexColumnParams + "\\)$")) {
                pgsqlCompareTableInfo.addModifyIndex(pgsqlIndexMetadata);
            }
        }

        // 需要删除的索引
        Set<String> needRemoveIndexes = pgsqlDbIndexMap.keySet();
        if (!needRemoveIndexes.isEmpty()) {
            pgsqlCompareTableInfo.addDropIndexes(needRemoveIndexes);
        }
    }

    private void compareColumnInfo(PgsqlTableMetadata tableMetadata, String tableName, PgsqlCompareTableInfo pgsqlCompareTableInfo) {
        // 数据库字段元信息
        List<PgsqlDbColumn> pgsqlDbColumns = executeReturn(pgsqlTablesMapper -> pgsqlTablesMapper.selectTableFieldDetail(tableName));
        Map<String, PgsqlDbColumn> pgsqlFieldDetailMap = pgsqlDbColumns.stream().collect(Collectors.toMap(PgsqlDbColumn::getColumnName, Function.identity()));
        // 当前字段信息
        List<PgsqlColumnMetadata> columnMetadataList = tableMetadata.getColumnMetadataList();

        for (PgsqlColumnMetadata pgsqlColumnMetadata : columnMetadataList) {
            String columnName = pgsqlColumnMetadata.getName();
            PgsqlDbColumn pgsqlDbColumn = pgsqlFieldDetailMap.remove(columnName);
            // 新增字段
            if (pgsqlDbColumn == null) {
                // 标记注释
                pgsqlCompareTableInfo.addColumnComment(pgsqlColumnMetadata.getName(), pgsqlColumnMetadata.getComment());
                // 标记字段信息
                pgsqlCompareTableInfo.addNewColumn(pgsqlColumnMetadata);
                continue;
            }
            // 修改了字段注释
            if (!Objects.equals(pgsqlDbColumn.getDescription(), pgsqlColumnMetadata.getComment())) {
                pgsqlCompareTableInfo.addColumnComment(columnName, pgsqlColumnMetadata.getComment());
            }
            /* 修改的字段 */
            String columnDefault = pgsqlDbColumn.getColumnDefault();

            // 主键忽略判断，单独处理
            if (!pgsqlColumnMetadata.isPrimary()) {
                // 字段类型不同
                boolean isTypeDiff = isTypeDiff(pgsqlColumnMetadata, pgsqlDbColumn);
                // 非null不同
                boolean isNotnullDiff = pgsqlColumnMetadata.isNotNull() == Objects.equals(pgsqlDbColumn.getIsNullable(), "YES");
                // 默认值不同
                boolean isDefaultDiff = isDefaultDiff(pgsqlColumnMetadata, columnDefault);
                if (isTypeDiff || isNotnullDiff || isDefaultDiff) {
                    pgsqlCompareTableInfo.addModifyColumn(pgsqlColumnMetadata);
                }
            }
        }
        // 需要删除的字段
        Set<String> needRemoveColumns = pgsqlFieldDetailMap.keySet();
        if (!needRemoveColumns.isEmpty()) {
            pgsqlCompareTableInfo.addDropColumns(needRemoveColumns);
        }

        /* 处理主键 */
        // 获取所有主键
        List<PgsqlColumnMetadata> primaryColumnList = columnMetadataList.stream().filter(PgsqlColumnMetadata::isPrimary).collect(Collectors.toList());
        // 查询数据库主键信息
        PgsqlDbPrimary pgsqlDbPrimary = executeReturn(pgsqlTablesMapper -> pgsqlTablesMapper.selectPrimaryKeyName(tableName));

        boolean removePrimary = primaryColumnList.isEmpty() && pgsqlDbPrimary != null;
        String newPrimaryColumns = primaryColumnList.stream().map(PgsqlColumnMetadata::getName).collect(Collectors.joining(","));
        boolean primaryChange = pgsqlDbPrimary != null && !Objects.equals(pgsqlDbPrimary.getColumns(), newPrimaryColumns);
        if (removePrimary || primaryChange) {
            // 标记待删除的主键
            pgsqlCompareTableInfo.setDropPrimaryKeyName(pgsqlDbPrimary.getPrimaryName());
        }
        boolean newPrimary = !primaryColumnList.isEmpty() && pgsqlDbPrimary == null;
        if (newPrimary || primaryChange) {
            // 标记新创建的主键
            pgsqlCompareTableInfo.addNewPrimary(primaryColumnList);
        }
    }

    private static boolean isTypeDiff(PgsqlColumnMetadata pgsqlColumnMetadata, PgsqlDbColumn pgsqlDbColumn) {
        String dataTypeFormat = pgsqlDbColumn.getDataTypeFormat().toLowerCase();
        String fullType = pgsqlColumnMetadata.getType().getFullType().toLowerCase();
        // 数字类型的，默认没有长度，但是数据库查询出来的有长度。 "int4(32)".startWith("int4")
        if (dataTypeFormat.startsWith("int")) {
            return !dataTypeFormat.startsWith(fullType);
        }
        return !Objects.equals(fullType, dataTypeFormat);
    }

    private static boolean isDefaultDiff(PgsqlColumnMetadata pgsqlColumnMetadata, String columnDefault) {

        // 纠正default值，去掉标记符号
        if (columnDefault != null && columnDefault.matches("^'.*'::character varying$")) {
            columnDefault = columnDefault.replace("::character varying", "");
        } else if (columnDefault != null && columnDefault.matches("^'.*'::bpchar$")) {
            columnDefault = columnDefault.replace("::bpchar", "");
        } else if (columnDefault != null && columnDefault.matches("^'.*'::date$")) {
            columnDefault = columnDefault.replace("::date", "");
        } else if (columnDefault != null && columnDefault.matches("^'.*'::time without time zone$")) {
            columnDefault = columnDefault.replace("::time without time zone", "");
        } else if (columnDefault != null && columnDefault.matches("^'.*'::timestamp without time zone$")) {
            columnDefault = columnDefault.replace("::timestamp without time zone", "");
        }

        DefaultValueEnum defaultValueType = pgsqlColumnMetadata.getDefaultValueType();

        if (DefaultValueEnum.isValid(defaultValueType)) {
            if (defaultValueType == DefaultValueEnum.EMPTY_STRING) {
                return !"''".equals(columnDefault);
            }
            if (defaultValueType == DefaultValueEnum.NULL) {
                return columnDefault != null;
            }
        } else {
            String defaultValue = pgsqlColumnMetadata.getDefaultValue();
            // if (!StringUtils.hasText(defaultValue)) {
            //     return false;
            // }

            return !Objects.equals(defaultValue, columnDefault);
        }
        return false;
    }

    private void compareTableInfo(PgsqlTableMetadata tableMetadata, String tableName, PgsqlCompareTableInfo pgsqlCompareTableInfo) {
        String tableDescription = executeReturn(pgsqlTablesMapper -> pgsqlTablesMapper.selectTableDescription(tableName));
        if (!Objects.equals(tableDescription, tableMetadata.getComment())) {
            pgsqlCompareTableInfo.setComment(tableMetadata.getComment());
        }
    }

    @Override
    public void modifyTable(PgsqlCompareTableInfo pgsqlCompareTableInfo) {
        String buildSql = ModifyTableSqlBuilder.buildSql(pgsqlCompareTableInfo);
        log.info("开始修改表：{}", pgsqlCompareTableInfo.getName());
        log.info("执行SQL：{}", buildSql);
        execute(pgsqlTablesMapper -> pgsqlTablesMapper.executeSql(buildSql));
        log.info("结束修改表：{}", pgsqlCompareTableInfo.getName());
    }
}
