package com.tangzc.autotable.core.dynamicds.impl;

import com.tangzc.autotable.core.dynamicds.IDataSourceHandler;
import com.tangzc.autotable.core.dynamicds.SqlSessionFactoryManager;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * @author don
 */

public class DefaultDataSourceHandler implements IDataSourceHandler<String> {

    @Override
    public void useDataSource(String dataSourceName) {
        // nothing
    }

    @Override
    public void clearDataSource(String dataSourceName) {
        // nothing
    }

    @Override
    public String getDataSourceName(Class<?> clazz) {
        return "";
    }
}
