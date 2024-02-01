package com.tangzc.autotable.core.dynamicds.impl;

import com.tangzc.autotable.core.dynamicds.IDatasourceHandler;
import com.tangzc.autotable.core.dynamicds.SqlSessionFactoryManager;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * @author don
 */

public class DefaultDatasourceHandler implements IDatasourceHandler<String> {

    @Override
    public SqlSessionFactory useDataSource(String dataSource) {
        // nothing
        return SqlSessionFactoryManager.getSqlSessionFactory();
    }

    @Override
    public void clearDataSource(String dataSource) {
        // nothing
    }

    @Override
    public String getDataSource(Class<?> clazz) {
        return "";
    }
}
