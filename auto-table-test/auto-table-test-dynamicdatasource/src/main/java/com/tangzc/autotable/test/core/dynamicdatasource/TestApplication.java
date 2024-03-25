package com.tangzc.autotable.test.core.dynamicdatasource;

import com.tangzc.autotable.core.AutoTableBootstrap;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.RunMode;
import com.tangzc.autotable.core.config.PropertyConfig;

/**
 * 多数据源测试
 */
public class TestApplication {

    public static void main(String[] args) {

        // 配置信息
        PropertyConfig autoTableProperties = new PropertyConfig();
        // 开启 删除不存在的列
        autoTableProperties.setAutoDropColumn(true);
        // 创建模式
        autoTableProperties.setMode(RunMode.create);
        AutoTableGlobalConfig.setAutoTableProperties(autoTableProperties);

        // 设置数据源处理器
        AutoTableGlobalConfig.setDatasourceHandler(new DynamicDataSourceHandler());

        AutoTableBootstrap.start();
    }
}
