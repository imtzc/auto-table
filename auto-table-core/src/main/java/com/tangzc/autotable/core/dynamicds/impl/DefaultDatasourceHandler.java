package com.tangzc.autotable.core.dynamicds.impl;

import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.constants.DatabaseDialect;
import com.tangzc.autotable.core.dynamicds.IDatasourceHandler;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * @author don
 */

public class DefaultDatasourceHandler implements IDatasourceHandler {

    @Override
    public void useDataSource(String dataSource) {
        // nothing
    }

    @Override
    public void clearDataSource(String dataSource) {
        // nothing
    }

    @Override
    public String getDataSource(Class<?> clazz) {
        return "";
    }

    @Override
    public DatabaseDialect getDatabaseDialect() {

        String databaseDialect = this.getDatabaseDialect(AutoTableGlobalConfig.getSqlSessionFactory());
        // 获取当前数据源所属的方言
        return DatabaseDialect.parseFromDialectName(databaseDialect);
    }

    private String getDatabaseDialect(SqlSessionFactory sqlSessionFactory) {
        // 获取Configuration对象
        Configuration configuration = sqlSessionFactory.getConfiguration();

        try (Connection connection = configuration.getEnvironment().getDataSource().getConnection()) {
            // 通过连接获取DatabaseMetaData对象
            DatabaseMetaData metaData = connection.getMetaData();
            // 获取数据库方言
            return metaData.getDatabaseProductName().toLowerCase();
        } catch (SQLException e) {
            throw new RuntimeException("获取数据方言失败", e);
        }
    }
}
