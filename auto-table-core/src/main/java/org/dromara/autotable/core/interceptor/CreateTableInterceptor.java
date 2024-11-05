package org.dromara.autotable.core.interceptor;

import org.dromara.autotable.core.strategy.TableMetadata;

/**
 * 建表之前拦截器
 * @author don
 */
@FunctionalInterface
public interface CreateTableInterceptor {

    /**
     * 建表前拦截
     *
     * @param databaseDialect 数据库方言
     * @param tableMetadata   表元数据
     */
    void beforeCreateTable(String databaseDialect, final TableMetadata tableMetadata);
}
