package com.tangzc.autotable.core.callback;

import com.tangzc.autotable.core.strategy.CompareTableInfo;

/**
 * 验证完回调
 */
@FunctionalInterface
public interface ValidateFinishCallback {

    /**
     * 验证完回调
     *
     * @param status           验证结果
     * @param tableClass       实体模型class
     * @param databaseDialect  数据库方言
     * @param compareTableInfo 对比表信息
     */
    void validateFinish(boolean status, Class<?> tableClass, String databaseDialect, final CompareTableInfo compareTableInfo);
}
