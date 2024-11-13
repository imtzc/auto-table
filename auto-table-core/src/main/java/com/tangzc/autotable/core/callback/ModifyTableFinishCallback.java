package com.tangzc.autotable.core.callback;

import com.tangzc.autotable.core.strategy.CompareTableInfo;
import com.tangzc.autotable.core.strategy.TableMetadata;

/**
 * 修改表回调
 * @author don
 */
@FunctionalInterface
public interface ModifyTableFinishCallback {

    /**
     * 修改表后回调
     *
     * @param databaseDialect  数据库方言
     * @param tableMetadata    表元数据
     * @param compareTableInfo 对比表信息
     */
    void afterModifyTable(String databaseDialect, final TableMetadata tableMetadata, final CompareTableInfo compareTableInfo);
}
