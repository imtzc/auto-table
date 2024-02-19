package com.tangzc.autotable.core.strategy.mysql.data;

import com.tangzc.autotable.annotation.enums.DefaultValueEnum;
import com.tangzc.autotable.core.converter.DatabaseTypeAndLength;
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
public class MysqlColumnMetadata {

    /**
     * 列名: 不可变，变了意味着新列
     */
    private String name;

    /**
     * 字段的备注
     */
    private String comment;

    /**
     * 字段类型
     */
    private DatabaseTypeAndLength type;

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
     * 默认字符集
     */
    private String characterSet;
    /**
     * 默认排序规则
     */
    private String collate;

    /**
     * 当前字段的顺序位置，按照实体字段自上而下排列的，父类的字段整体排在子类之后
     */
    private int position;

    /**
     * <p>表示前一列的列名:
     * <p>if 非空，生成“AFTER [列名]”，表示位于某列之后；
     * <p>else if 空字符，生成“FIRST”，表示第一列；
     * <p>else 表示没有变动；
     */
    private String newPreColumn;

    /**
     * 生成字段相关的SQL片段
     */
    public String toColumnSql() {
        // 例子：`name` varchar(100) NULL DEFAULT '张三' COMMENT '名称'
        // 例子：`id` int(32) NOT NULL AUTO_INCREMENT COMMENT '主键'
        return StringConnectHelper.newInstance("`{columnName}` {typeAndLength} {character} {collate} {null} {default} {autoIncrement} {columnComment} {position}")
                .replace("{columnName}", this.getName())
                .replace("{typeAndLength}", MysqlTypeHelper.getFullType(this.type))
                .replace("{character}", $ -> {
                    if (StringUtils.hasText(this.getCharacterSet())) {
                        return "CHARACTER SET " + this.getCharacterSet();
                    }
                    return "";
                })
                .replace("{collate}", $ -> {
                    if (StringUtils.hasText(this.getCollate())) {
                        return "COLLATE " + this.getCollate();
                    }
                    return "";
                })
                .replace("{null}", this.isNotNull() ? "NOT NULL" : "NULL")
                .replace("{default}", $ -> {
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
                .replace("{autoIncrement}", this.isAutoIncrement() ? "AUTO_INCREMENT" : "")
                .replace("{columnComment}", StringUtils.hasText(this.getComment()) ? "COMMENT '" + this.getComment() + "'" : "")
                .replace("{position}", $ -> {
                    if (StringUtils.hasText(this.newPreColumn)) {
                        return "AFTER `" + this.newPreColumn + "`";
                    }
                    if ("".equals(this.newPreColumn)) {
                        return "FIRST";
                    }
                    return "";
                })
                .toString();
    }
}
