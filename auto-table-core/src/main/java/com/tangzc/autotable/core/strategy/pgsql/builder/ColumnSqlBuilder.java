package com.tangzc.autotable.core.strategy.pgsql.builder;

import com.tangzc.autotable.annotation.enums.DefaultValueEnum;
import com.tangzc.autotable.core.strategy.ColumnMetadata;
import com.tangzc.autotable.core.strategy.pgsql.data.PgsqlTypeHelper;
import com.tangzc.autotable.core.utils.StringConnectHelper;
import com.tangzc.autotable.core.utils.StringUtils;

/**
 * 列相关的SQL生成器
 * @author don
 */
public class ColumnSqlBuilder {

    /**
     * 生成字段相关的SQL片段
     * @param columnMetadata 列元数据
     * @return 列相关的sql
     */
    public static String buildSql(ColumnMetadata columnMetadata) {
        // 例子："name" varchar(100) NULL DEFAULT '张三' COMMENT '名称'
        // 例子："id" int4(32) NOT NULL AUTO_INCREMENT COMMENT '主键'
        return StringConnectHelper.newInstance("{columnName} {typeAndLength} {null} {default}")
                .replace("{columnName}", columnMetadata.getName())
                .replace("{typeAndLength}", () -> {
                    /* 如果是自增，忽略指定的类型，交给pgsql自动处理，pgsql会设定int4(32)类型，
                    并自动生成一个序列：表名_字段名_seq，同时设置字段的默认值为：nextval('表名_字段名_seq'::regclass) */
                    if (columnMetadata.isAutoIncrement()) {
                        return "serial";
                    }
                    return PgsqlTypeHelper.getFullType(columnMetadata.getType());
                })
                .replace("{null}", columnMetadata.isNotNull() ? "NOT NULL" : "")
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
