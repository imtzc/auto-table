package com.tangzc.autotable.core.strategy.sqlite.data;

import com.tangzc.autotable.annotation.enums.DefaultValueEnum;
import com.tangzc.autotable.core.utils.StringConnectHelper;
import com.tangzc.autotable.core.utils.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 用于存放创建表的字段信息
 *
 * @author don
 */
@Slf4j
@Data
public class SqliteColumnMetadata {

    /**
     * 字段名: 不可变，变了意味着新字段
     */
    private String name;

    /**
     * 字段的备注
     */
    private String comment;

    /**
     * 字段类型
     */
    private SqliteTypeAndLength type;

    /**
     * 字段是否非空
     */
    private boolean notNull;

    /**
     * 主键是否自增
     */
    private boolean autoIncrement;

    /**
     * <p>字段默认值类型</p>
     * <p>如果该值有值的情况下，将忽略 {@link #defaultValue} 的值</p>
     */
    private DefaultValueEnum defaultValueType;

    /**
     * <p>字段默认值</p>
     * <p>如果 {@link #defaultValueType} 有值的情况下，将忽略本字段的指定</p>
     */
    private String defaultValue;

    /**
     * 字段是否是主键
     */
    private boolean primary;

    /**
     * 生成字段相关的SQL片段
     * "id" INTEGER NOT NULL AUTOINCREMENT, -- 主键
     * "name" TEXT(200) NOT NULL DEFAULT '', -- 姓名
     * "age" INTEGER(2), -- 年龄
     * "address" TEXT(500) DEFAULT 济南市, -- 地址
     * "card_id" INTEGER(11) NOT NULL, -- 身份证id
     * "card_number" text(30) NOT NULL -- 身份证号码
     */
    public String toColumnSql(boolean isSinglePrimaryKey, boolean addComma) {
        return StringConnectHelper.newInstance("\"{columnName}\" {typeAndLength} {null} {default} {primaryKey}{comma}{columnComment}")
                .replace("{columnName}", this.getName())
                .replace("{typeAndLength}", this.type.getFullType())
                .replace("{null}", this.isNotNull() ? "NOT NULL" : "NULL")
                .replace("{default}", (key) -> {
                    // 指定NULL
                    DefaultValueEnum defaultValueType = this.getDefaultValueType();
                    if (defaultValueType == DefaultValueEnum.NULL) {
                        return "DEFAULT NULL";
                    }
                    // 指定空字符串
                    if (defaultValueType == DefaultValueEnum.EMPTY_STRING) {
                        return "DEFAULT ''";
                    }
                    // 自定义
                    String defaultValue = this.getDefaultValue();
                    if (DefaultValueEnum.isCustom(defaultValueType) && StringUtils.hasText(defaultValue)) {
                        return "DEFAULT " + defaultValue;
                    }
                    return "";
                })
                .replace("{primaryKey}", (key) -> {
                    // sqlite特殊：只能是一个主键的情况下，才能设置自增，且只有主键才能自增
                    if (isSinglePrimaryKey && this.isPrimary() && this.isAutoIncrement()) {
                        return "PRIMARY KEY AUTOINCREMENT";
                    }
                    return "";
                })
                .replace("{comma}", addComma ? "," : "")
                .replace("{columnComment}", StringUtils.hasText(this.getComment()) ? " -- " + this.getComment() : "")
                .toString();
    }
}
