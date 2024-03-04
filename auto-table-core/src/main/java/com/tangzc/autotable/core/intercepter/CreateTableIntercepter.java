package com.tangzc.autotable.core.intercepter;

import com.tangzc.autotable.core.strategy.TableMetadata;

/**
 * 表信息拦截器
 */
@FunctionalInterface
public interface CreateTableIntercepter {

    /**
     * 建表前拦截
     *
     * @param tableClass      实体模型class
     * @param databaseDialect 数据库方言
     * @param tableMetadata   表元数据
     */
    void beforeCreateTable(Class<?> tableClass, String databaseDialect, final TableMetadata tableMetadata);
}
