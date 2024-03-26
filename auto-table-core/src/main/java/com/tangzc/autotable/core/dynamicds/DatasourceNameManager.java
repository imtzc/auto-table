package com.tangzc.autotable.core.dynamicds;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * 记录数据源名称
 */
@Slf4j
public class DatasourceNameManager {

    /**
     * 当前数据源
     */
    private static final ThreadLocal<String> datasourceName = new ThreadLocal<>();

    public static void setDatasourceName(@NonNull String datasourceName) {
        DatasourceNameManager.datasourceName.set(datasourceName);
    }

    public static String getDatasourceName() {
        String datasourceName = DatasourceNameManager.datasourceName.get();
        if (datasourceName == null) {
            log.error("当前数据源下，未找到对应的DatasourceName");
        }
        return datasourceName;
    }

    public static void cleanDatasourceName() {
        datasourceName.remove();
    }
}
