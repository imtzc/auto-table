package com.tangzc.autotable.core.intercepter;

import com.tangzc.autotable.core.strategy.CompareTableInfo;
import com.tangzc.autotable.core.strategy.TableMetadata;

/**
 * 修改表前拦截器
 */
@FunctionalInterface
public interface ModifyTableIntercepter {

    /**
     * 修改表前拦截
     *
     * @param tableClass       实体模型class
     * @param databaseDialect  数据库方言
     * @param tableMetadata    表元数据
     * @param compareTableInfo 对比表信息
     */
    void beforeModifyTable(Class<?> tableClass, String databaseDialect, final TableMetadata tableMetadata, final CompareTableInfo compareTableInfo);
}
