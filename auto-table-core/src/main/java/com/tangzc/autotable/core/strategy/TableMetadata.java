package com.tangzc.autotable.core.strategy;

import lombok.Getter;
import lombok.Setter;

/**
 * Bean的基础信息元数据
 *
 * @author don
 */
@Getter
public abstract class TableMetadata {

    /**
     * 表名
     */
    protected String tableName;

    /**
     * 注释
     */
    @Setter
    protected String comment;

    public TableMetadata(String tableName) {
        this.tableName = tableName;
    }
}
