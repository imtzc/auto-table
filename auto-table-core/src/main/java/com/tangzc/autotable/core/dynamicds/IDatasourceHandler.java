package com.tangzc.autotable.core.dynamicds;

import com.tangzc.autotable.core.constants.DatabaseDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @author don
 */
public interface IDatasourceHandler {

    Logger log = LoggerFactory.getLogger(IDatasourceHandler.class);

    /**
     * 开始分析处理模型
     * 处理ignore and repeat表
     *
     * @param classList 待处理的类
     */
    default void handleAnalysis(Set<Class<?>> classList, BiConsumer<DatabaseDialect, Set<Class<?>>> consumer) {

        // <数据源，Set<表>>
        Map<String, Set<Class<?>>> needHandleTableMap = classList.stream()
                .collect(Collectors.groupingBy(this::getDataSource, Collectors.toSet()));

        needHandleTableMap.forEach((dataSource, tables) -> {
            this.useDataSource(dataSource);
            try {
                DatabaseDialect databaseDialect = this.getDatabaseDialect();
                if (databaseDialect != null) {
                    consumer.accept(databaseDialect, tables);
                }
            } finally {
                this.clearDataSource(dataSource);
            }
        });

    }

    /**
     * 开始使用指定的数据源
     * @param dataSource 数据源名称
     */
    void useDataSource(String dataSource);

    /**
     * 清除指定的数据源
     * @param dataSource 数据源名称
     */
    void clearDataSource(String dataSource);

    /**
     * 获取指定类的数据库数据源
     *
     * @param clazz 指定类
     * @return 数据源名称
     */
    String getDataSource(Class<?> clazz);

    /**
     * 自动获取当前数据源的方言
     *
     * @return 返回数据方言
     */
    DatabaseDialect getDatabaseDialect();
}
