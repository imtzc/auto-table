package com.tangzc.autotable.core.strategy.mysql.builder;

import com.tangzc.autotable.core.strategy.IndexMetadata;
import com.tangzc.autotable.core.strategy.mysql.data.MysqlColumnMetadata;
import com.tangzc.autotable.core.strategy.mysql.data.MysqlCompareTableInfo;
import com.tangzc.autotable.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author don
 */
@Slf4j
public class ModifyTableSqlBuilder {

    /**
     * 构建创建新表的SQL
     *
     * @param mysqlCompareTableInfo 参数
     * @return sql
     */
    public static String buildSql(MysqlCompareTableInfo mysqlCompareTableInfo) {

        String name = mysqlCompareTableInfo.getName();

        String collate = mysqlCompareTableInfo.getCollate();
        String engine = mysqlCompareTableInfo.getEngine();
        String characterSet = mysqlCompareTableInfo.getCharacterSet();
        String comment = mysqlCompareTableInfo.getComment();

        List<String> dropColumnList = mysqlCompareTableInfo.getDropColumnList();
        List<MysqlCompareTableInfo.MysqlModifyColumnMetadata> modifyMysqlColumnMetadataList = mysqlCompareTableInfo.getModifyMysqlColumnMetadataList();
        List<String> dropIndexList = mysqlCompareTableInfo.getDropIndexList();
        List<IndexMetadata> mysqlIndexMetadataList = mysqlCompareTableInfo.getIndexMetadataList();

        // 记录所有修改项，（利用数组结构，便于添加,分割）
        List<String> modifyItems = new ArrayList<>();

        // 删除表字段处理
        modifyItems.add(
                dropColumnList.stream()
                        .map(dropColumn -> "DROP COLUMN `{columnName}`"
                                .replace("{columnName}", dropColumn)
                        ).collect(Collectors.joining(","))
        );

        // 拼接每个字段的sql片段
        modifyItems.add(
                modifyMysqlColumnMetadataList.stream()
                        .sorted(Comparator.comparingInt(modifyColumn -> modifyColumn.getMysqlColumnMetadata().getPosition()))
                        .map(modifyColumn -> {
                            MysqlColumnMetadata columnMetadata = modifyColumn.getMysqlColumnMetadata();
                            // 判断是主键，自动设置为NOT NULL，并记录
                            if (columnMetadata.isPrimary()) {
                                columnMetadata.setNotNull(true);
                            }
                            String columnSql = ColumnSqlBuilder.buildSql(columnMetadata);

                            if (modifyColumn.getType() == MysqlCompareTableInfo.ModifyType.MODIFY) {
                                // 修改表字段处理
                                return "MODIFY COLUMN " + columnSql;
                            } else {
                                // 新增表字段处理
                                return "ADD COLUMN " + columnSql;
                            }
                        })
                        .collect(Collectors.joining(","))
        );

        /*
        处理主键
         */
        // 判断是否需要删除原有主键
        if (mysqlCompareTableInfo.isDropPrimary()) {
            modifyItems.add("DROP PRIMARY KEY");
        }
        // 判断是否存在新的主键，添加
        if (!mysqlCompareTableInfo.getNewPrimaries().isEmpty()) {
            List<String> primaries = mysqlCompareTableInfo.getNewPrimaries().stream()
                    .map(MysqlColumnMetadata::getName)
                    .collect(Collectors.toList());
            String primaryKeySql = CreateTableSqlBuilder.getPrimaryKeySql(primaries);
            modifyItems.add("ADD " + primaryKeySql);
        }

        // 删除索引
        modifyItems.add(
                dropIndexList.stream()
                        .map(dropIndex -> "DROP INDEX `{indexName}`"
                                .replace("{indexName}", dropIndex))
                        .collect(Collectors.joining(","))
        );

        // 添加索引
        modifyItems.add(
                mysqlIndexMetadataList.stream().map(indexParam -> {
                    String indexSql = CreateTableSqlBuilder.getIndexSql(indexParam);
                    return "ADD " + indexSql;
                }).collect(Collectors.joining(","))
        );

        // 引擎，相较于新增表，多了","前缀
        if (StringUtils.hasText(engine)) {
            modifyItems.add("ENGINE = " + engine);
        }
        // 字符集，相较于新增表，多了","前缀
        if (StringUtils.hasText(characterSet)) {
            modifyItems.add("CHARACTER SET = " + characterSet);
        }
        // 排序，相较于新增表，多了","前缀
        if (StringUtils.hasText(collate)) {
            modifyItems.add("COLLATE = " + collate);
        }
        // 备注，相较于新增表，多了","前缀
        if (StringUtils.hasText(comment)) {
            modifyItems.add(
                    "COMMENT = '{comment}'"
                            .replace("{comment}", comment)
            );
        }

        // 组合sql: 过滤空字符项，逗号拼接
        String modifySql = modifyItems.stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(","));

        return "ALTER TABLE `{tableName}` {modifyItems};"
                .replace("{tableName}", name)
                .replace("{modifyItems}", modifySql);
    }
}
