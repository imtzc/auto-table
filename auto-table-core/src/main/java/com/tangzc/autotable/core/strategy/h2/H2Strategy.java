package com.tangzc.autotable.core.strategy.h2;

import com.tangzc.autotable.core.constants.DatabaseDialect;
import com.tangzc.autotable.core.converter.DefaultTypeEnumInterface;
import com.tangzc.autotable.core.strategy.DefaultTableMetadata;
import com.tangzc.autotable.core.strategy.IStrategy;
import com.tangzc.autotable.core.strategy.h2.builder.CreateTableSqlBuilder;
import com.tangzc.autotable.core.strategy.h2.builder.H2TableMetadataBuilder;
import com.tangzc.autotable.core.strategy.h2.data.H2CompareTableInfo;
import com.tangzc.autotable.core.strategy.h2.data.H2DefaultTypeEnum;
import com.tangzc.autotable.core.strategy.h2.mapper.H2TablesMapper;
import com.tangzc.autotable.core.strategy.mysql.data.MysqlCompareTableInfo;
import com.tangzc.autotable.core.strategy.mysql.data.MysqlTableMetadata;
import com.tangzc.autotable.core.strategy.mysql.data.dbdata.InformationSchemaStatistics;
import com.tangzc.autotable.core.strategy.mysql.data.dbdata.InformationSchemaTable;
import com.tangzc.autotable.core.utils.StringUtils;
import lombok.NonNull;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class H2Strategy implements IStrategy<DefaultTableMetadata, H2CompareTableInfo, H2TablesMapper> {
    @Override
    public String databaseDialect() {
        return DatabaseDialect.H2;
    }

    @Override
    public Map<Class<?>, DefaultTypeEnumInterface> typeMapping() {
        return new HashMap<Class<?>, DefaultTypeEnumInterface>(32) {{
            put(String.class, H2DefaultTypeEnum.VARCHAR_IGNORECASE);
            put(Character.class, H2DefaultTypeEnum.CHAR);
            put(char.class, H2DefaultTypeEnum.CHAR);

            put(byte.class, H2DefaultTypeEnum.TINYINT);
            put(Byte.class, H2DefaultTypeEnum.TINYINT);
            put(short.class, H2DefaultTypeEnum.SMARTINT);
            put(Short.class, H2DefaultTypeEnum.SMARTINT);
            put(int.class, H2DefaultTypeEnum.INT);
            put(Integer.class, H2DefaultTypeEnum.INT);
            put(long.class, H2DefaultTypeEnum.BIGINT);
            put(Long.class, H2DefaultTypeEnum.BIGINT);

            put(float.class, H2DefaultTypeEnum.REAL);
            put(Float.class, H2DefaultTypeEnum.REAL);
            put(double.class, H2DefaultTypeEnum.DOUBLE);
            put(Double.class, H2DefaultTypeEnum.DOUBLE);
            put(BigDecimal.class, H2DefaultTypeEnum.DECIMAL);

            put(Boolean.class, H2DefaultTypeEnum.BOOLEAN);

            put(Time.class, H2DefaultTypeEnum.TIME);
            put(LocalTime.class, H2DefaultTypeEnum.TIME);
            put(Date.class, H2DefaultTypeEnum.DATE);
            put(LocalDate.class, H2DefaultTypeEnum.DATE);
            put(java.util.Date.class, H2DefaultTypeEnum.TIMESTAMP);
            put(LocalDateTime.class, H2DefaultTypeEnum.TIMESTAMP);
        }};
    }

    @Override
    public String dropTable(String schema, String tableName) {
        return String.format("DROP TABLE IF EXISTS `%s`", tableName);
    }

    @Override
    public @NonNull DefaultTableMetadata analyseClass(Class<?> beanClass) {
        return new H2TableMetadataBuilder().build(beanClass);
    }

    @Override
    public List<String> createTable(DefaultTableMetadata tableMetadata) {
        String createTableSql = CreateTableSqlBuilder.buildSql(tableMetadata);
        return Collections.singletonList(createTableSql);
    }

    @Override
    public @NonNull H2CompareTableInfo compareTable(DefaultTableMetadata tableMetadata) {
        String tableName = tableMetadata.getTableName();
        String schema = tableMetadata.getSchema();
        H2CompareTableInfo compareTableInfo = new H2CompareTableInfo(tableName, schema);

        // InformationSchemaTable informationSchemaTable = executeReturn(mapper -> mapper.findTableByTableName(tableName));
        //
        // // 对比表配置有无变化
        // compareTableProperties(tableMetadata, informationSchemaTable, compareTableInfo);
        //
        // // 开始比对列的变化: 新增、修改、删除
        // compareColumns(tableMetadata, tableName, compareTableInfo);
        //
        // // 开始比对 主键 和 索引 的变化
        // List<InformationSchemaStatistics> informationSchemaStatistics = executeReturn(mysqlTablesMapper -> mysqlTablesMapper.queryTablePrimaryAndIndex(tableName));
        // // 按照主键（固定值：PRIMARY）、索引名字，对所有列进行分组
        // Map<String, List<InformationSchemaStatistics>> keyColumnGroupByName = informationSchemaStatistics.stream()
        //         .collect(Collectors.groupingBy(InformationSchemaStatistics::getIndexName));
        //
        // // 对比主键
        // List<InformationSchemaStatistics> tablePrimaries = keyColumnGroupByName.remove("PRIMARY");
        // comparePrimary(tableMetadata, compareTableInfo, tablePrimaries);
        //
        // // 对比索引, informationSchemaKeyColumnUsages中剩余的都是索引数据了
        // Map<String, List<InformationSchemaStatistics>> tableIndexes = keyColumnGroupByName;
        // compareIndexes(tableMetadata, compareTableInfo, tableIndexes);

        return compareTableInfo;
    }

    private static void compareTableProperties(DefaultTableMetadata defaultTableMetadata, InformationSchemaTable tableInformation, MysqlCompareTableInfo mysqlCompareTableInfo) {
        String tableComment = defaultTableMetadata.getComment();
        // 判断表注释是否要更新
        if (StringUtils.hasText(tableComment) && !tableComment.equals(tableInformation.getTableComment())) {
            mysqlCompareTableInfo.setComment(tableComment);
        }
    }

    @Override
    public List<String> modifyTable(H2CompareTableInfo compareTableInfo) {
        return null;
    }

    public static String withSchemaName(String schema, String name) {
        return StringUtils.hasText(schema) ? (schema + "." + name) : name;
    }
}
