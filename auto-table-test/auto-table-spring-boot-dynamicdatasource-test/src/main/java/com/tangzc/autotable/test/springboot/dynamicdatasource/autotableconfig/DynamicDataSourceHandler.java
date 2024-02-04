package com.tangzc.autotable.test.springboot.dynamicdatasource.autotableconfig;

import com.tangzc.autotable.core.dynamicds.IDataSourceHandler;
import com.tangzc.autotable.test.springboot.dynamicdatasource.dynamicdatasourceframe.Ds;
import com.tangzc.autotable.test.springboot.dynamicdatasource.dynamicdatasourceframe.DynamicDataSourceContextHolder;
import org.springframework.stereotype.Component;

@Component
public class DynamicDataSourceHandler implements IDataSourceHandler<String> {

    @Override
    public void useDataSource(String dataSourceName) {
        // 切换数据源
        DynamicDataSourceContextHolder.setContextKey(dataSourceName);
    }

    @Override
    public void clearDataSource(String dataSourceName) {
        // 清除数据源
        DynamicDataSourceContextHolder.removeContextKey();
    }

    @Override
    public String getDataSourceName(Class<?> clazz) {
        // 根据实体类获取对应的数据源名称，假定自定义的多数据源，有一个注解Ds
        Ds ds = clazz.getAnnotation(Ds.class);
        if (ds != null) {
            return ds.value();
        } else {
            return DynamicDataSourceContextHolder.getContextKey();
        }
    }
}
