package com.tangzc.autotable.core.dynamicds;

import lombok.NonNull;
import org.apache.ibatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
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
     */
    default void handleAnalysis(Set<Class<?>> classList, BiConsumer<String, Set<Class<?>>> consumer) {

        // <数据源，Set<表>>
        Map<T, Set<Class<?>>> needHandleTableMap = classList.stream()
                .collect(Collectors.groupingBy(this::getDataSourceName, Collectors.toSet()));

        needHandleTableMap.forEach((dataSource, entityClass) -> {
            // 使用数据源
            this.useDataSource(dataSource);
            try {
                String databaseDialect = this.getDatabaseDialect();
                consumer.accept(databaseDialect, entityClass);
            } finally {
                this.clearDataSource(dataSource);
            }
        });

    }

    /**
     * 自动获取当前数据源的方言
     *
     * @return 返回数据方言
     */
    default String getDatabaseDialect() {

        // 获取Configuration对象
        Configuration configuration = SqlSessionFactoryManager.getSqlSessionFactory().getConfiguration();

        try (Connection connection = configuration.getEnvironment().getDataSource().getConnection()) {
            // 通过连接获取DatabaseMetaData对象
            DatabaseMetaData metaData = connection.getMetaData();
            log.info("数据库链接 => {}", metaData.getURL());
            // 获取数据库方言
            return metaData.getDatabaseProductName();
        } catch (SQLException e) {
            throw new RuntimeException("获取数据方言失败", e);
        }
    }

    /**
     * 切换指定的数据源
     * @param dataSourceName 数据源名称
     */
    void useDataSource(T dataSourceName);

    /**
     * 清除当前数据源
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
