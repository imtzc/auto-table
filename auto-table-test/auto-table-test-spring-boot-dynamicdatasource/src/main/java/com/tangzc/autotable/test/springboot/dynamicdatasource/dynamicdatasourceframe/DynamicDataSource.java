package com.tangzc.autotable.test.springboot.dynamicdatasource.dynamicdatasourceframe;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author don
 */
public class DynamicDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicDataSourceContextHolder.getContextKey();
    }
}
