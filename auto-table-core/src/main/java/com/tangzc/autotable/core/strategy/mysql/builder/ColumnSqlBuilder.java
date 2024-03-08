package com.tangzc.autotable.core.strategy.mysql.builder;

import com.tangzc.autotable.annotation.enums.DefaultValueEnum;
import com.tangzc.autotable.core.strategy.mysql.data.MysqlColumnMetadata;
import com.tangzc.autotable.core.strategy.mysql.data.MysqlTypeHelper;
import com.tangzc.autotable.core.utils.StringConnectHelper;
import com.tangzc.autotable.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * @author don
 */
@Slf4j
public class ColumnSqlBuilder {

    /**
     * 生成字段相关的SQL片段
     */
    public static String buildSql(MysqlColumnMetadata columnMetadata) {
        // 例子：`name` varchar(100) NULL DEFAULT '张三' COMMENT '名称'
        // 例子：`id` int(32) NOT NULL AUTO_INCREMENT COMMENT '主键'
        return StringConnectHelper.newInstance("`{columnName}` {typeAndLength} {character} {collate} {null} {default} {autoIncrement} {columnComment} {position}")
                .replace("{columnName}", columnMetadata.getName())
                .replace("{typeAndLength}", MysqlTypeHelper.getFullType(columnMetadata.getType()))
                .replace("{character}", $ -> {
                    if (StringUtils.hasText(columnMetadata.getCharacterSet())) {
                        return "CHARACTER SET " + columnMetadata.getCharacterSet();
                    }
                    return "";
                })
                .replace("{collate}", $ -> {
                    if (StringUtils.hasText(columnMetadata.getCollate())) {
                        return "COLLATE " + columnMetadata.getCollate();
                    }
                    return "";
                })
                .replace("{null}", columnMetadata.isNotNull() ? "NOT NULL" : "NULL")
                .replace("{default}", $ -> {
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
                .replace("{position}", $ -> {
                    if (StringUtils.hasText(columnMetadata.getNewPreColumn())) {
                        return "AFTER `" + columnMetadata.getNewPreColumn() + "`";
                    }
                    if ("".equals(columnMetadata.getNewPreColumn())) {
                        return "FIRST";
                    }
                    return "";
                })
                .toString();
    }
}
