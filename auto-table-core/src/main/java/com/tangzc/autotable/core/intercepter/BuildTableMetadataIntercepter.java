package com.tangzc.autotable.core.intercepter;

import com.tangzc.autotable.core.strategy.TableMetadata;

/**
 * 表信息拦截器
 * 注解构建完表元信息后，执行拦截器
 */
@FunctionalInterface
public interface BuildTableMetadataIntercepter {

    /**
     * 拦截器
     *
     * @param databaseDialect 数据库方言：MySQL、PostgreSQL、SQLite
     * @param tableMetadata   表元数据：MysqlTableMetadata、DefaultTableMetadata、DefaultTableMetadata
     */
    void intercept(final String databaseDialect, final TableMetadata tableMetadata);
}
