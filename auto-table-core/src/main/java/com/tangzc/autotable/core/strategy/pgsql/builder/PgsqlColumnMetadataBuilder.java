package com.tangzc.autotable.core.strategy.pgsql.builder;

import com.tangzc.autotable.annotation.ColumnDefault;
import com.tangzc.autotable.core.builder.ColumnMetadataBuilder;
import com.tangzc.autotable.core.constants.DatabaseDialect;
import com.tangzc.autotable.core.converter.DatabaseTypeAndLength;
import com.tangzc.autotable.core.strategy.pgsql.data.PgsqlTypeHelper;
import com.tangzc.autotable.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 用于存放创建表的字段信息
 *
 * @author don
 */
@Slf4j
public class PgsqlColumnMetadataBuilder extends ColumnMetadataBuilder {

    public PgsqlColumnMetadataBuilder() {
        super(DatabaseDialect.PostgreSQL);
    }

    @Override
    protected String getDefaultValue(DatabaseTypeAndLength typeAndLength, ColumnDefault columnDefault) {

        String defaultValue = super.getDefaultValue(typeAndLength, columnDefault);

        if (StringUtils.hasText(defaultValue)) {
            // 布尔值，自动转化
            if (PgsqlTypeHelper.isBoolean(typeAndLength)) {
                if ("1".equals(defaultValue)) {
                    defaultValue = "true";
                } else if ("0".equals(defaultValue)) {
                    defaultValue = "false";
                }
            }
            // 兼容逻辑：如果是字符串的类型，自动包一层''（如果没有的话）
            if (PgsqlTypeHelper.isCharString(typeAndLength) && !defaultValue.startsWith("'") && !defaultValue.endsWith("'")) {
                defaultValue = "'" + defaultValue + "'";
            }
            // 兼容逻辑：如果是日期，且非函数，自动包一层''（如果没有的话）
            if (PgsqlTypeHelper.isTime(typeAndLength) && defaultValue.matches("(\\d+.?)+") && !defaultValue.startsWith("'") && !defaultValue.endsWith("'")) {
                defaultValue = "'" + defaultValue + "'";
            }
        }
        return defaultValue;
    }
}
