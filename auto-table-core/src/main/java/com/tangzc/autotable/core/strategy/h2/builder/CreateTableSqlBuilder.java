package com.tangzc.autotable.core.strategy.h2.builder;

import com.tangzc.autotable.annotation.enums.DefaultValueEnum;
import com.tangzc.autotable.annotation.enums.IndexTypeEnum;
import com.tangzc.autotable.core.strategy.ColumnMetadata;
import com.tangzc.autotable.core.strategy.DefaultTableMetadata;
import com.tangzc.autotable.core.strategy.IndexMetadata;
import com.tangzc.autotable.core.strategy.h2.H2Strategy;
import com.tangzc.autotable.core.strategy.mysql.data.MysqlTypeHelper;
import com.tangzc.autotable.core.utils.StringConnectHelper;
import com.tangzc.autotable.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author don
 */
@Slf4j
public class CreateTableSqlBuilder {

    /**
     * 构建创建新表的SQL
     *
     * @param defaultTableMetadata 参数
     * @return sql
     */
    public static String buildSql(DefaultTableMetadata defaultTableMetadata) {

        String name = defaultTableMetadata.getTableName();
        String schema = defaultTableMetadata.getSchema();
        List<ColumnMetadata> mysqlColumnMetadataList = defaultTableMetadata.getColumnMetadataList();
        List<IndexMetadata> indexMetadataList = defaultTableMetadata.getIndexMetadataList();
        String comment = defaultTableMetadata.getComment();

        // 记录所有修改项，（利用数组结构，便于添加,分割）
        List<String> addItems = new ArrayList<>();

        // 获取所有主键（至于表字段处理之前，为了主键修改notnull）
        List<String> primaries = new ArrayList<>();
        mysqlColumnMetadataList.forEach(columnData -> {
            // 判断是主键，自动设置为NOT NULL，并记录
            if (columnData.isPrimary()) {
                columnData.setNotNull(true);
                primaries.add(columnData.getName());
            }
        });

        // 表字段处理
        addItems.add(
                mysqlColumnMetadataList.stream()
                        // 拼接每个字段的sql片段
                        .map(CreateTableSqlBuilder::getColumnSql)
                        .collect(Collectors.joining(","))
        );


        // 主键
        if (!primaries.isEmpty()) {
            String primaryKeySql = getPrimaryKeySql(primaries);
            addItems.add(primaryKeySql);
        }

        // 索引
        addItems.add(
                indexMetadataList.stream()
                        // 例子： UNIQUE INDEX `unique_name_age`(`name` ASC, `age` DESC) COMMENT '姓名、年龄索引' USING BTREE
                        .map(CreateTableSqlBuilder::getIndexSql)
                        // 同类型的索引，排在一起，SQL美化
                        .sorted()
                        .collect(Collectors.joining(","))
        );

        // 备注
        String propertiesSql = "";
        if (StringUtils.hasText(comment)) {
            propertiesSql = "COMMENT = '{comment}'"
                    .replace("{comment}", comment);
        }

        // 组合sql: 过滤空字符项，逗号拼接
        String addSql = addItems.stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(","));

        return "CREATE TABLE `{tableName}` ({addItems}) {tableProperties};"
                .replace("{tableName}", H2Strategy.withSchemaName(schema, name))
                .replace("{addItems}", addSql)
                .replace("{tableProperties}", propertiesSql);
    }


    /**
     * 生成字段相关的SQL片段
     */
    public static String getColumnSql(ColumnMetadata columnMetadata) {
        // 例子：`name` varchar(100) NULL DEFAULT '张三' COMMENT '名称'
        // 例子：`id` int(32) NOT NULL AUTO_INCREMENT COMMENT '主键'
        return StringConnectHelper.newInstance("`{columnName}` {typeAndLength} {character} {collate} {null} {default} {autoIncrement} {columnComment} {position}")
                .replace("{columnName}", columnMetadata.getName())
                .replace("{typeAndLength}", MysqlTypeHelper.getFullType(columnMetadata.getType()))
                .replace("{null}", columnMetadata.isNotNull() ? "NOT NULL" : "NULL")
                .replace("{default}", () -> {
                    // 指定NULL
                    DefaultValueEnum defaultValueType = columnMetadata.getDefaultValueType();
                    if (defaultValueType == DefaultValueEnum.NULL) {
                        return "DEFAULT NULL";
                    }
                    // 指定空字符串
                    if (defaultValueType == DefaultValueEnum.EMPTY_STRING) {
                        return "DEFAULT ''";
                    }
                    // 自定义
                    String defaultValue = columnMetadata.getDefaultValue();
                    if (DefaultValueEnum.isCustom(defaultValueType) && StringUtils.hasText(defaultValue)) {
                        return "DEFAULT " + defaultValue;
                    }
                    return "";
                })
                .replace("{autoIncrement}", columnMetadata.isAutoIncrement() ? "AUTO_INCREMENT" : "")
                .replace("{columnComment}", StringUtils.hasText(columnMetadata.getComment()) ? "COMMENT '" + columnMetadata.getComment() + "'" : "")
                .toString();
    }

    public static String getIndexSql(IndexMetadata indexMetadata) {
        // 例子： UNIQUE INDEX `unique_name_age`(`name` ASC, `age` DESC) COMMENT '姓名、年龄索引',
        return StringConnectHelper.newInstance("{indexType} INDEX `{indexName}`({columns}) {indexComment}")
                .replace("{indexType}", indexMetadata.getType() == IndexTypeEnum.UNIQUE ? "UNIQUE" : "")
                .replace("{indexName}", indexMetadata.getName())
                .replace("{columns}", () -> {
                    List<IndexMetadata.IndexColumnParam> columnParams = indexMetadata.getColumns();
                    return columnParams.stream().map(column ->
                            // 例：`name` ASC
                            "`{column}` {sortMode}"
                                    .replace("{column}", column.getColumn())
                                    .replace("{sortMode}", column.getSort() != null ? column.getSort().name() : "")
                    ).collect(Collectors.joining(","));
                })
                .replace("{indexComment}", StringUtils.hasText(indexMetadata.getComment()) ? "COMMENT '" + indexMetadata.getComment() + "'" : "")
                .toString();
    }

    public static String getPrimaryKeySql(List<String> primaries) {
        return "PRIMARY KEY ({primaries})"
                .replace(
                        "{primaries}",
                        primaries.stream()
                                .map(fieldName -> "`" + fieldName + "`")
                                .collect(Collectors.joining(","))
                );
    }
}
