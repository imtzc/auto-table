package com.tangzc.autotable.springboot;

import com.tangzc.autotable.core.AutoTableBootstrap;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.dynamicds.IDataSourceHandler;
import com.tangzc.autotable.core.dynamicds.SqlSessionFactoryManager;
import com.tangzc.autotable.springboot.properties.AutoTableProperties;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@AutoConfigureAfter({DataSourceAutoConfiguration.class, MybatisAutoConfiguration.class})
public class AutoTableAutoConfig {

    private final SqlSessionTemplate sqlSessionTemplate;
    private final AutoTableProperties autoTableProperties;

    @Autowired(required = false)
    private IDataSourceHandler<?> dynamicDataSourceHandler;

    public AutoTableAutoConfig(SqlSessionTemplate sqlSessionTemplate, AutoTableProperties autoTableProperties) {
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.autoTableProperties = autoTableProperties;
    }

    @PostConstruct
    public void init() {

        // 设置全局的配置
        AutoTableGlobalConfig.setAutoTableProperties(autoTableProperties.toConfig());
        // 默认设置全局的SqlSessionFactory
        SqlSessionFactoryManager.setSqlSessionFactory(sqlSessionTemplate.getSqlSessionFactory());
        // 有自定义多数据源处理逻辑，就使用多数据源模式
        if (dynamicDataSourceHandler != null) {
            AutoTableGlobalConfig.setDatasourceHandler(dynamicDataSourceHandler);
        }
        // 启动AutoTable
        AutoTableBootstrap.start();
    }
}
