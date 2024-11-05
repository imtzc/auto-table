package org.dromara.autotable.core.callback;

import org.dromara.autotable.core.strategy.CompareTableInfo;

/**
 * 验证完回调
 */
@FunctionalInterface
public interface ValidateFinishCallback {

    /**
     * 验证完回调
     *
     * @param status           验证结果
     * @param databaseDialect  数据库方言
     * @param compareTableInfo 对比表信息
     */
    void validateFinish(boolean status, String databaseDialect, final CompareTableInfo compareTableInfo);
}
