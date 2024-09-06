package com.tangzc.autotable.core.strategy.h2;

import com.tangzc.autotable.annotation.enums.DefaultValueEnum;
import com.tangzc.autotable.annotation.enums.IndexTypeEnum;
import com.tangzc.autotable.annotation.h2.H2TypeConstant;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.constants.DatabaseDialect;
import com.tangzc.autotable.core.converter.DatabaseTypeAndLength;
import com.tangzc.autotable.core.converter.DefaultTypeEnumInterface;
import com.tangzc.autotable.core.strategy.ColumnMetadata;
import com.tangzc.autotable.core.strategy.DefaultTableMetadata;
import com.tangzc.autotable.core.strategy.IStrategy;
import com.tangzc.autotable.core.strategy.IndexMetadata;
import com.tangzc.autotable.core.strategy.h2.builder.CreateTableSqlBuilder;
import com.tangzc.autotable.core.strategy.h2.builder.H2TableMetadataBuilder;
import com.tangzc.autotable.core.strategy.h2.builder.ModifyTableSqlBuilder;
import com.tangzc.autotable.core.strategy.h2.data.H2CompareTableInfo;
import com.tangzc.autotable.core.strategy.h2.data.H2DefaultTypeEnum;
import com.tangzc.autotable.core.strategy.h2.data.H2TypeHelper;
import com.tangzc.autotable.core.strategy.h2.data.dbdata.InformationSchemaColumns;
import com.tangzc.autotable.core.strategy.h2.data.dbdata.InformationSchemaIndexes;
import com.tangzc.autotable.core.strategy.h2.data.dbdata.InformationSchemaTables;
import com.tangzc.autotable.core.strategy.h2.mapper.H2TablesMapper;
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
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
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
            put(Character.class, H2DefaultTypeEnum.CHARACTER);
            put(char.class, H2DefaultTypeEnum.CHARACTER);

            put(byte.class, H2DefaultTypeEnum.TINYINT);
            put(Byte.class, H2DefaultTypeEnum.TINYINT);
            put(short.class, H2DefaultTypeEnum.SMARTINT);
            put(Short.class, H2DefaultTypeEnum.SMARTINT);
            put(int.class, H2DefaultTypeEnum.INTEGER);
            put(Integer.class, H2DefaultTypeEnum.INTEGER);
            put(long.class, H2DefaultTypeEnum.BIGINT);
            put(Long.class, H2DefaultTypeEnum.BIGINT);

            put(float.class, H2DefaultTypeEnum.REAL);
            put(Float.class, H2DefaultTypeEnum.REAL);
            put(double.class, H2DefaultTypeEnum.NUMERIC);
            put(Double.class, H2DefaultTypeEnum.NUMERIC);
            put(BigDecimal.class, H2DefaultTypeEnum.NUMERIC);

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
        // 删除表，并同时删除外键
        return String.format("DROP TABLE IF EXISTS `%s` CASCADE", tableName);
    }

    @Override
    public @NonNull DefaultTableMetadata analyseClass(Class<?> beanClass) {
        return new H2TableMetadataBuilder().build(beanClass);
    }

    @Override
    public List<String> createTable(DefaultTableMetadata tableMetadata) {
        return CreateTableSqlBuilder.buildColumnSql(tableMetadata);
    }

    @Override
    public @NonNull H2CompareTableInfo compareTable(DefaultTableMetadata tableMetadata) {
        String tableName = tableMetadata.getTableName();
        String schema = tableMetadata.getSchema();
        H2CompareTableInfo h2CompareTableInfo = new H2CompareTableInfo(tableName, schema);

        // 比较表信息
        compareTableInfo(tableMetadata, h2CompareTableInfo);

        // 比较字段信息
        compareColumnInfo(tableMetadata, h2CompareTableInfo);

        // 比较索引信息
        compareIndexInfo(tableMetadata, h2CompareTableInfo);

        return h2CompareTableInfo;
    }

    private void compareIndexInfo(DefaultTableMetadata tableMetadata, H2CompareTableInfo h2CompareTableInfo) {

        String tableName = tableMetadata.getTableName();
        String schema = tableMetadata.getSchema();

        List<InformationSchemaIndexes> informationSchemaIndexes = executeReturn(pgsqlTablesMapper -> pgsqlTablesMapper.findIndexInformation(schema, tableName));
        Map<String, List<InformationSchemaIndexes>> indexMap = informationSchemaIndexes.stream()
                .collect(Collectors.groupingBy(InformationSchemaIndexes::getIndexName));

        List<IndexMetadata> indexMetadataList = tableMetadata.getIndexMetadataList();
        for (IndexMetadata indexMetadata : indexMetadataList) {
            String indexName = indexMetadata.getName();
            String comment = indexMetadata.getComment();
            // 尝试从索引标记集合中删除索引
            List<InformationSchemaIndexes> dbIndex = indexMap.remove(indexName);

            /* 新增索引 */
            boolean isNewIndex = dbIndex == null;
            // 删除失败，表示是新增的索引
            if (isNewIndex) {
                // 标记索引信息
                h2CompareTableInfo.addNewIndex(indexMetadata);
                // 标记注释
                if (StringUtils.hasText(comment)) {
                    h2CompareTableInfo.addIndexComment(indexName, comment);
                }
                continue;
            }

            /* 修改索引 */
            // 可能是复合索引，取出第一个字段，即可拿到大部分信息（因为都是相同的）
            InformationSchemaIndexes firstIndexColumns = dbIndex.get(0);

            // 索引注释改变
            if (!Objects.equals(firstIndexColumns.getRemarks(), comment)) {
                h2CompareTableInfo.addIndexComment(indexName, comment);
            }

            // 判断索引是否一致
            boolean isUniqueIndex = indexMetadata.getType() == IndexTypeEnum.UNIQUE;
            boolean dbIsUniqueIndex = firstIndexColumns.getIsUnique();
            // 索引类型改变了
            if (isUniqueIndex != dbIsUniqueIndex) {
                h2CompareTableInfo.addModifyIndex(indexMetadata);
                continue;
            }
            // 索引字段数量改变了
            if (indexMetadata.getColumns().size() != dbIndex.size()) {
                h2CompareTableInfo.addModifyIndex(indexMetadata);
                continue;
            }

            String indexColumnStr = indexMetadata.getColumns().stream().map(index -> index.getColumn().toUpperCase() + " " + index.getSort().name()).collect(Collectors.joining(","));
            String dbIndexColumnStr = dbIndex.stream().map(index -> index.getColumnName() + " " + index.getOrderingSpecification()).collect(Collectors.joining(","));
            // 索引字段或者顺序改变了
            if (!indexColumnStr.equals(dbIndexColumnStr)) {
                h2CompareTableInfo.addModifyIndex(indexMetadata);
            }
        }

        // 需要删除的索引
        Set<String> needRemoveIndexes = indexMap.keySet();
        if (!needRemoveIndexes.isEmpty()) {
            // 根据配置，决定是否删除库上的多余索引
            if (AutoTableGlobalConfig.getAutoTableProperties().getAutoDropIndex()) {
                h2CompareTableInfo.addDropIndexes(needRemoveIndexes);
            }
        }
    }

    private void compareColumnInfo(DefaultTableMetadata tableMetadata, H2CompareTableInfo h2CompareTableInfo) {

        String tableName = tableMetadata.getTableName();
        String schema = tableMetadata.getSchema();
        // 数据库字段元信息
        List<InformationSchemaColumns> informationSchemaColumns = executeReturn(mapper -> mapper.findColumnInformation(schema, tableName));
        Map<String, InformationSchemaColumns> pgsqlFieldDetailMap = informationSchemaColumns.stream().collect(Collectors.toMap(InformationSchemaColumns::getColumnName, Function.identity()));
        // 当前字段信息
        List<ColumnMetadata> columnMetadataList = tableMetadata.getColumnMetadataList();

        for (ColumnMetadata columnMetadata : columnMetadataList) {
            String columnName = columnMetadata.getName().toUpperCase();
            InformationSchemaColumns schemaColumns = pgsqlFieldDetailMap.remove(columnName);
            // 新增字段
            if (schemaColumns == null) {
                // 标记注释
                h2CompareTableInfo.addColumnComment(columnMetadata.getName(), columnMetadata.getComment());
                // 标记字段信息
                h2CompareTableInfo.addNewColumn(columnMetadata);
                continue;
            }
            // 修改了字段注释
            if (!Objects.equals(schemaColumns.getRemarks(), columnMetadata.getComment())) {
                h2CompareTableInfo.addColumnComment(columnName, columnMetadata.getComment());
            }
            /* 修改的字段 */
            // 字段类型不同
            boolean isTypeDiff = isTypeDiff(columnMetadata, schemaColumns);
            // 非null不同
            boolean isNotnullDiff = columnMetadata.isNotNull() != Objects.equals(schemaColumns.getIsNullable(), "NO");
            // 默认值不同
            boolean isDefaultDiff = isDefaultDiff(columnMetadata, schemaColumns);
            // 自增不同
            boolean isAutoIncrement = columnMetadata.isAutoIncrement() != schemaColumns.autoIncrement();
            if (isTypeDiff || isNotnullDiff || isDefaultDiff || isAutoIncrement) {
                h2CompareTableInfo.addModifyColumn(columnMetadata);
            }
            // 主键不同
            boolean isPrimaryDiff = columnMetadata.isPrimary() != schemaColumns.primaryKey();
            if(isPrimaryDiff) {
                h2CompareTableInfo.addNewPrimary(columnMetadata);
            }
        }
        // 需要删除的字段
        Set<String> needRemoveColumns = pgsqlFieldDetailMap.keySet();
        if (!needRemoveColumns.isEmpty()) {
            // 根据配置，决定是否删除库上的多余字段
            if (AutoTableGlobalConfig.getAutoTableProperties().getAutoDropColumn()) {
                h2CompareTableInfo.addDropColumns(needRemoveColumns);
            }
        }
    }

    private boolean isTypeDiff(ColumnMetadata columnMetadata, InformationSchemaColumns informationSchemaColumns) {
        String dbColumnType = informationSchemaColumns.getDataType();
        DatabaseTypeAndLength columnMetadataType = columnMetadata.getType();

        /* 比较字符串 */
        boolean columnIsCharString = H2TypeConstant.CHARACTER_VARYING.equals(dbColumnType);
        boolean fieldIsCharString = H2TypeHelper.isCharString(columnMetadataType);
        if (columnIsCharString != fieldIsCharString) {
            // 字段类型不同
            return false;
        }
        if (columnIsCharString && fieldIsCharString) {
            Long characterMaximumLength = informationSchemaColumns.getCharacterMaximumLength();
            // 字段长度不同.
            // 先判断注解上是否指定了长度，没有指定的情况下，数据库有默认长度，如果直接比较肯定不同，但是改了也没有意义。
            // 此处的最佳方案是枚举的长度上配置上默认的长度，但是偷懒，就算了
            if (columnMetadataType.getLength() != null && !Objects.equals(characterMaximumLength, Long.valueOf(columnMetadataType.getLength()))) {
                return false;
            }
        }

        /* 比较数字 */
        boolean columnIsNumber = H2TypeHelper.isNumber(dbColumnType);
        boolean fieldIsNumber = H2TypeHelper.isNumber(columnMetadataType);
        if (columnIsNumber != fieldIsNumber) {
            // 字段类型不同
            return false;
        }
        if (columnIsNumber && fieldIsNumber) {
            Integer numericPrecision = informationSchemaColumns.getNumericPrecision();
            Integer numericScale = informationSchemaColumns.getNumericScale();
            // 字段长度不同
            // 先判断注解上是否指定了长度，没有指定的情况下，数据库有默认长度，如果直接比较肯定不同，但是改了也没有意义。
            // 此处的最佳方案是枚举的长度上配置上默认的长度，但是偷懒，就算了
            if ((columnMetadataType.getLength() != null && !Objects.equals(numericPrecision, columnMetadataType.getLength())) ||
                    (columnMetadataType.getDecimalLength() != null && !Objects.equals(numericScale, columnMetadataType.getDecimalLength()))) {
                return false;
            }
        }

        // 比较其他类型
        String fieldType = columnMetadataType.getType().toUpperCase();
        return Objects.equals(dbColumnType, fieldType);
    }

    private boolean isDefaultDiff(ColumnMetadata columnMetadata, InformationSchemaColumns schemaColumns) {

        String columnDefault = schemaColumns.getColumnDefault();

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

    private void compareTableInfo(DefaultTableMetadata tableMetadata, H2CompareTableInfo h2CompareTableInfo) {

        String tableName = tableMetadata.getTableName();
        String schema = tableMetadata.getSchema();

        InformationSchemaTables informationSchemaTables = executeReturn(mapper -> mapper.findTableInformation(schema, tableName));
        if (!Objects.equals(informationSchemaTables.getRemarks(), tableMetadata.getComment())) {
            h2CompareTableInfo.setComment(tableMetadata.getComment());
        }
    }

    @Override
    public List<String> modifyTable(H2CompareTableInfo compareTableInfo) {
        return ModifyTableSqlBuilder.buildSql(compareTableInfo);
    }

    public static String withSchemaName(String schema, String name) {
        return StringUtils.hasText(schema) ? (schema + "." + name) : name;
    }
}
