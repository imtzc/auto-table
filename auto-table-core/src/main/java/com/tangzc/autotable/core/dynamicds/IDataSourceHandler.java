package com.tangzc.autotable.core.dynamicds;

import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author don
 */
public interface IDataSourceHandler<T extends Serializable> {

    Logger log = LoggerFactory.getLogger(IDataSourceHandler.class);

    /**
     * 开始分析处理模型
     * 处理ignore and repeat表
     *
     * @param classList 待处理的类
     * @param consumer  实体消费回调
     */
    default void handleAnalysis(Set<Class<?>> classList, Consumer<Set<Class<?>>> consumer) {

        // <数据源，Set<表>>
        Map<T, Set<Class<?>>> needHandleTableMap = classList.stream()
                .collect(Collectors.groupingBy(this::getDataSourceName, Collectors.toSet()));

        needHandleTableMap.forEach((dataSource, entityClasses) -> {
            // 使用数据源
            log.info("使用数据源：{}", dataSource);
            this.useDataSource(dataSource);
            try {
                consumer.accept(entityClasses);
            } finally {
                log.info("清理数据源：{}", dataSource);
                this.clearDataSource(dataSource);
            }
        });

    }

    /**
     * 切换指定的数据源
     *
     * @param dataSourceName 数据源名称
     */
    void useDataSource(T dataSourceName);

    /**
     * 清除当前数据源
     *
     * @param dataSourceName 数据源名称
     */
    void clearDataSource(T dataSourceName);

    /**
     * 获取指定类的数据库数据源
     *
     * @param clazz 指定类
     * @return 数据源名称，表分组的依据，届时，根据该值分组所有的表，同一数据源下的统一处理
     */
    @NonNull T getDataSourceName(Class<?> clazz);
}
