package com.tangzc.autotable.core.dynamicds;

import com.tangzc.autotable.core.constants.DatabaseDialect;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;

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
public interface IDatasourceHandler<T extends Serializable> {

    /**
     * 开始分析处理模型
     * 处理ignore and repeat表
     *
     * @param classList 待处理的类
     */
    default void handleAnalysis(Set<Class<?>> classList, BiConsumer<DatabaseDialect, Set<Class<?>>> consumer) {

        // <数据源，Set<表>>
        Map<T, Set<Class<?>>> needHandleTableMap = classList.stream()
                .collect(Collectors.groupingBy(this::getDataSource, Collectors.toSet()));

        needHandleTableMap.forEach((dataSource, tables) -> {
            // 切换数据源
            SqlSessionFactory sqlSessionFactory = this.useDataSource(dataSource);
            // 设置SqlSessionFactory
            SqlSessionFactoryManager.setSqlSessionFactory(sqlSessionFactory);
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
     * 自动获取当前数据源的方言
     *
     * @return 返回数据方言
     */
    default DatabaseDialect getDatabaseDialect() {

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
     * 开始使用指定的数据源
     * @param dataSource 数据源名称
     */
    SqlSessionFactory useDataSource(T dataSource);

    /**
     * 清除指定的数据源
     * @param dataSource 数据源名称
     */
    void clearDataSource(T dataSource);

    /**
     * 获取指定类的数据库数据源
     *
     * @param clazz 指定类
     * @return 数据源名称
     */
    T getDataSource(Class<?> clazz);
}
