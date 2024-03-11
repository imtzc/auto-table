package com.tangzc.autotable.core.dynamicds.impl;

import com.tangzc.autotable.core.dynamicds.IDataSourceHandler;
import lombok.NonNull;

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
    public @NonNull String getDataSourceName(Class<?> clazz) {
        return "";
    }
}
