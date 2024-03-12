package com.tangzc.autotable.core.strategy.sqlite;

import com.tangzc.autotable.core.constants.DatabaseDialect;
import com.tangzc.autotable.core.converter.DefaultTypeEnumInterface;
import com.tangzc.autotable.core.strategy.DefaultTableMetadata;
import com.tangzc.autotable.core.strategy.IStrategy;
import com.tangzc.autotable.core.strategy.IndexMetadata;
import com.tangzc.autotable.core.strategy.sqlite.builder.CreateTableSqlBuilder;
import com.tangzc.autotable.core.strategy.sqlite.builder.SqliteTableMetadataBuilder;
import com.tangzc.autotable.core.strategy.sqlite.data.SqliteCompareTableInfo;
import com.tangzc.autotable.core.strategy.sqlite.data.SqliteDefaultTypeEnum;
import com.tangzc.autotable.core.strategy.sqlite.data.dbdata.SqliteMaster;
import com.tangzc.autotable.core.strategy.sqlite.mapper.SqliteTablesMapper;
import com.tangzc.autotable.core.utils.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author don
 */
public class SqliteStrategy implements IStrategy<DefaultTableMetadata, SqliteCompareTableInfo, SqliteTablesMapper> {

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public String databaseDialect() {
        return DatabaseDialect.SQLite;
    }

    @Override
    public Map<Class<?>, DefaultTypeEnumInterface> typeMapping() {
        return new HashMap<Class<?>, DefaultTypeEnumInterface>() {{
            put(String.class, SqliteDefaultTypeEnum.TEXT);
            put(Character.class, SqliteDefaultTypeEnum.TEXT);
            put(char.class, SqliteDefaultTypeEnum.TEXT);

            put(BigInteger.class, SqliteDefaultTypeEnum.INTEGER);
            put(Long.class, SqliteDefaultTypeEnum.INTEGER);
            put(long.class, SqliteDefaultTypeEnum.INTEGER);

            put(Integer.class, SqliteDefaultTypeEnum.INTEGER);
            put(int.class, SqliteDefaultTypeEnum.INTEGER);

            put(Boolean.class, SqliteDefaultTypeEnum.INTEGER);
            put(boolean.class, SqliteDefaultTypeEnum.INTEGER);

            put(Float.class, SqliteDefaultTypeEnum.REAL);
            put(float.class, SqliteDefaultTypeEnum.REAL);
            put(Double.class, SqliteDefaultTypeEnum.REAL);
            put(double.class, SqliteDefaultTypeEnum.REAL);
            put(BigDecimal.class, SqliteDefaultTypeEnum.REAL);

            put(Date.class, SqliteDefaultTypeEnum.TEXT);
            put(java.sql.Date.class, SqliteDefaultTypeEnum.TEXT);
            put(java.sql.Timestamp.class, SqliteDefaultTypeEnum.TEXT);
            put(java.sql.Time.class, SqliteDefaultTypeEnum.TEXT);
            put(LocalDateTime.class, SqliteDefaultTypeEnum.TEXT);
            put(LocalDate.class, SqliteDefaultTypeEnum.TEXT);
            put(LocalTime.class, SqliteDefaultTypeEnum.TEXT);

            put(Short.class, SqliteDefaultTypeEnum.INTEGER);
            put(short.class, SqliteDefaultTypeEnum.INTEGER);
        }};
    }

    @Override
    public void dropTable(String tableName) {
        execute(sqliteTablesMapper -> sqliteTablesMapper.dropTableByName(tableName));
    }

    @Override
    public boolean checkTableExist(String tableName) {
        int i = executeReturn(sqliteTablesMapper -> sqliteTablesMapper.checkTableExist(tableName));
        return i > 0;
    }

    @Override
    public DefaultTableMetadata analyseClass(Class<?> beanClass) {
        DefaultTableMetadata tableMetadata = SqliteTableMetadataBuilder.build(beanClass);
        if (tableMetadata.getColumnMetadataList().isEmpty()) {
            log.warn("扫描发现{}没有建表字段请检查！", beanClass.getName());
            return null;
        }
        return tableMetadata;
    }

    @Override
    public void createTable(DefaultTableMetadata tableMetadata) {
        String createTableSql = CreateTableSqlBuilder.buildTableSql(tableMetadata.getTableName(), tableMetadata.getComment(), tableMetadata.getColumnMetadataList());
        log.info("执行SQL：{}", createTableSql);
        execute(sqliteTablesMapper -> sqliteTablesMapper.executeSql(createTableSql));
        List<String> createIndexSqlList = CreateTableSqlBuilder.buildIndexSql(tableMetadata.getTableName(), tableMetadata.getIndexMetadataList());
        for (String createIndexSql : createIndexSqlList) {
            log.info("执行SQL：{}", createIndexSql);
            execute(sqliteTablesMapper -> sqliteTablesMapper.executeSql(createIndexSql));
        }
    }

    @Override
    public SqliteCompareTableInfo compareTable(DefaultTableMetadata tableMetadata) {

        String tableName = tableMetadata.getTableName();
        SqliteCompareTableInfo sqliteCompareTableInfo = new SqliteCompareTableInfo(tableName);

        // 判断表是否需要重建
        String orgBuildTableSql = executeReturn(sqliteTablesMapper -> sqliteTablesMapper.queryBuildTableSql(tableName));
        String newBuildTableSql = CreateTableSqlBuilder.buildTableSql(tableMetadata.getTableName(), tableMetadata.getComment(), tableMetadata.getColumnMetadataList());
        boolean needRebuildTable = !Objects.equals(orgBuildTableSql + ";", newBuildTableSql);
        if (needRebuildTable) {
            // 该情况下无需单独分析索引了，因为sqlite的表修改方式为重建整个表，索引需要全部删除，重新创建
            sqliteCompareTableInfo.setRebuildTableSql(newBuildTableSql);
            // 删除当前所有索引
            List<SqliteMaster> orgBuildIndexSqlList = executeReturn(sqliteTablesMapper -> sqliteTablesMapper.queryBuildIndexSql(tableName));
            for (SqliteMaster sqliteMaster : orgBuildIndexSqlList) {
                sqliteCompareTableInfo.getDeleteIndexList().add(sqliteMaster.getName());
            }
            // 添加新建索引的sql
            List<String> buildIndexSqlList = CreateTableSqlBuilder.buildIndexSql(tableName, tableMetadata.getIndexMetadataList());
            for (String buildIndexSql : buildIndexSqlList) {
                sqliteCompareTableInfo.getBuildIndexSqlList().add(buildIndexSql);
            }
        } else {
            // 不需要重建表的情况下，才有必要单独判断索引的更新情况
            // 判断索引是否需要重建 <索引name，索引sql>
            Map<String, String> rebuildIndexMap = tableMetadata.getIndexMetadataList().stream()
                    .collect(Collectors.toMap(
                            IndexMetadata::getName,
                            indexMetadata -> CreateTableSqlBuilder.getIndexSql(tableName, indexMetadata)
                    ));
            // 遍历所有数据库存在的索引，判断有没有变化
            List<SqliteMaster> orgBuildIndexSqlList = executeReturn(sqliteTablesMapper -> sqliteTablesMapper.queryBuildIndexSql(tableName));
            for (SqliteMaster sqliteMaster : orgBuildIndexSqlList) {
                String indexName = sqliteMaster.getName();
                String newBuildIndexSql = rebuildIndexMap.remove(indexName);
                boolean exit = newBuildIndexSql != null;
                // 如果最新构建标记上没有该注解的标记了，则说明该注解需要删除了
                if (!exit) {
                    sqliteCompareTableInfo.getDeleteIndexList().add(indexName);
                }
                // 新的索引构建语句中存在相同名称的索引，且内容不一致，需要重新构建
                String createIndexSqlRecord = sqliteMaster.getSql() + ";";
                if (exit && !Objects.equals(newBuildIndexSql, createIndexSqlRecord)) {
                    sqliteCompareTableInfo.getDeleteIndexList().add(indexName);
                    sqliteCompareTableInfo.getBuildIndexSqlList().add(newBuildIndexSql);
                }
            }
            // 筛选完，剩下的，是需要新增的索引
            Map<String, String> needNewIndexes = rebuildIndexMap;
            if (!needNewIndexes.isEmpty()) {
                sqliteCompareTableInfo.getBuildIndexSqlList().addAll(needNewIndexes.values());
            }
        }

        return sqliteCompareTableInfo;
    }

    @Override
    public void modifyTable(SqliteCompareTableInfo sqliteCompareTableInfo) {

        // 删除索引
        List<String> deleteIndexList = sqliteCompareTableInfo.getDeleteIndexList();
        if (!deleteIndexList.isEmpty()) {
            for (String deleteIndexName : deleteIndexList) {
                execute(sqliteTablesMapper -> sqliteTablesMapper.dropIndexSql(deleteIndexName));
            }
        }

        // 重建表
        String rebuildTableSql = sqliteCompareTableInfo.getRebuildTableSql();
        if (StringUtils.hasText(rebuildTableSql)) {
            String orgTableName = sqliteCompareTableInfo.getName();
            String backupTableName = getBackupTableName(orgTableName);
            // 备份表
            execute(sqliteTablesMapper -> sqliteTablesMapper.backupTable(orgTableName, backupTableName));
            // 重新建表
            execute(sqliteTablesMapper -> sqliteTablesMapper.executeSql(rebuildTableSql));
            // 迁移数据
            execute(sqliteTablesMapper -> sqliteTablesMapper.migrateData(orgTableName, backupTableName));
        }

        // 创建索引
        List<String> buildIndexSqlList = sqliteCompareTableInfo.getBuildIndexSqlList();
        if (!buildIndexSqlList.isEmpty()) {
            for (String buildIndexSql : buildIndexSqlList) {
                execute(sqliteTablesMapper -> sqliteTablesMapper.executeSql(buildIndexSql));
            }
        }
    }

    private String getBackupTableName(String orgTableName) {

        int offset = 0;
        String name = "_{orgTableName}_old_{datetime}"
                .replace("{orgTableName}", orgTableName)
                .replace("{datetime}", LocalDateTime.now().format(dateTimeFormatter));
        StringBuilder backupName = new StringBuilder(name);
        while (true) {
            if (offset > 0) {
                backupName.append("_").append(offset);
            }
            String finalBackupName = backupName.toString();
            int count = executeReturn(sqliteTablesMapper -> sqliteTablesMapper.checkTableExist(finalBackupName));
            if (count == 0) {
                return backupName.toString();
            } else {
                offset++;
            }
        }
    }
}
