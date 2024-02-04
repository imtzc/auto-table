package com.tangzc.autotable.core.dynamicds;

import com.sun.istack.internal.Nullable;
import com.tangzc.autotable.core.constants.DatabaseDialect;
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
    default void handleAnalysis(Set<Class<?>> classList, BiConsumer<DatabaseDialect, Set<Class<?>>> consumer) {

        // <数据源，Set<表>>
        Map<T, Set<Class<?>>> needHandleTableMap = classList.stream()
                .collect(Collectors.groupingBy(this::getDataSourceName, Collectors.toSet()));

        needHandleTableMap.forEach((dataSource, tables) -> {
            // 使用数据源
            this.useDataSource(dataSource);
            try {
                DatabaseDialect databaseDialect = this.getDatabaseDialect();
                if (databaseDialect != null) {
                    consumer.accept(databaseDialect, tables);
                } else {
                    log.warn("忽略处理以下实体：{}", tables.stream().map(Class::getName).collect(Collectors.joining(", ")));
                }
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
    default @Nullable DatabaseDialect getDatabaseDialect() {

        // 获取Configuration对象
        Configuration configuration = SqlSessionFactoryManager.getSqlSessionFactory().getConfiguration();

        try (Connection connection = configuration.getEnvironment().getDataSource().getConnection()) {
            // 通过连接获取DatabaseMetaData对象
            DatabaseMetaData metaData = connection.getMetaData();
            // 获取数据库方言
            String databaseDialect = metaData.getDatabaseProductName().toLowerCase();
            // 获取当前数据源所属的方言
            return DatabaseDialect.parseFromDialectName(databaseDialect);
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