package com.tangzc.autotable.core.strategy;

import lombok.Getter;

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

    public TableMetadata(String tableName) {
        this.tableName = tableName;
    }
}
