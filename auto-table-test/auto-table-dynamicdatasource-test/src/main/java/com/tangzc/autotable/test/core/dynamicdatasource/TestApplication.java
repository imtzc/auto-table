package com.tangzc.autotable.test.core.dynamicdatasource;

import com.tangzc.autotable.core.AutoTableBootstrap;
import com.tangzc.autotable.core.AutoTableGlobalConfig;

/**
 * 多数据源测试
 */
public class TestApplication {

    public static void main(String[] args) {

        // 配置信息
        AutoTableGlobalConfig.PropertyConfig autoTableProperties = new AutoTableGlobalConfig.PropertyConfig();
        // 开启 删除不存在的列
        autoTableProperties.setAutoDropColumn(true);
        AutoTableGlobalConfig.setAutoTableProperties(autoTableProperties);

        // 设置数据源处理器
        AutoTableGlobalConfig.setDatasourceHandler(new DynamicDataSourceHandler());

        AutoTableBootstrap.start();
    }
}
