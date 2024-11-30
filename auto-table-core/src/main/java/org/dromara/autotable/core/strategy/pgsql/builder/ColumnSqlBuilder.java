package org.dromara.autotable.core.strategy.pgsql.builder;

import org.dromara.autotable.annotation.enums.DefaultValueEnum;
import org.dromara.autotable.core.strategy.ColumnMetadata;
import org.dromara.autotable.core.utils.StringConnectHelper;
import org.dromara.autotable.core.utils.StringUtils;

/**
 * 列相关的SQL生成器
 *
 * @author don
 */
public class ColumnSqlBuilder {

    /**
     * 生成字段相关的SQL片段
     *
     * @param columnMetadata 列元数据
     * @return 列相关的sql
     */
    public static String buildSql(ColumnMetadata columnMetadata) {
        // 例子："name" varchar(100) NULL DEFAULT '张三' COMMENT '名称'
        // 例子："id" int4(32) NOT NULL AUTO_INCREMENT COMMENT '主键'
        StringConnectHelper sql = StringConnectHelper.newInstance("{columnName} {typeAndLength} {null} {default}")
                .replace("{columnName}", columnMetadata.getName())
                .replace("{typeAndLength}", () -> columnMetadata.getType().getDefaultFullType());

        // 如果是自增列，则使用GENERATED ALWAYS AS IDENTITY, 忽略not null和默认值的配置
        if (columnMetadata.isAutoIncrement()) {
            return sql.replace("{null} {default}", "GENERATED ALWAYS AS IDENTITY").toString();
        }

        return sql.replace("{null}", columnMetadata.isNotNull() ? "NOT NULL" : "")
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
                .toString();
    }
}
