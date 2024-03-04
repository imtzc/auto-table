package com.tangzc.autotable.core.intercepter;

import com.tangzc.autotable.core.strategy.TableMetadata;

/**
 * 表信息拦截器
 */
@FunctionalInterface
public interface BuildTableMetadataIntercepter {

    /**
     * 拦截器
     * @param databaseDialect 数据库方言：MySQL、PostgreSQL、SQLite
     * @param tableMetadata 表元数据：MysqlTableMetadata、PgsqlTableMetadata、SqliteTableMetadata
     */
    void intercept(final String databaseDialect, final TableMetadata tableMetadata);
}
