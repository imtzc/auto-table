package org.dromara.autotable.core.callback;

import org.dromara.autotable.core.strategy.TableMetadata;

/**
 * 建表回调
 * @author don
 */
@FunctionalInterface
public interface CreateTableFinishCallback {

    /**
     * 建表后回调
     *
     * @param databaseDialect 数据库方言
     * @param tableMetadata   表元数据
     */
    void afterCreateTable(String databaseDialect, final TableMetadata tableMetadata);
}
