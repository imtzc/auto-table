package com.tangzc.autotable.test.springboot.dynamicdatasource;

import com.tangzc.autotable.core.dynamicds.IDataSourceHandler;
import com.tangzc.autotable.test.springboot.dynamicdatasource.dynamicdatasourceframe.Ds;
import com.tangzc.autotable.test.springboot.dynamicdatasource.dynamicdatasourceframe.DynamicDataSourceContextHolder;
import org.springframework.stereotype.Component;

@Component
public class DynamicDataSourceHandler implements IDataSourceHandler<String> {

    @Override
    public void useDataSource(String dataSourceName) {
        DynamicDataSourceContextHolder.setContextKey(dataSourceName);
    }

    @Override
    public void clearDataSource(String dataSourceName) {
        DynamicDataSourceContextHolder.removeContextKey();
    }

    @Override
    public String getDataSourceName(Class<?> clazz) {
        Ds ds = clazz.getAnnotation(Ds.class);
        if (ds != null) {
            return ds.value();
        } else {
            return DynamicDataSourceContextHolder.getContextKey();
        }
    }
}
