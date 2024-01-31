package com.tangzc.autotable.spring.boot;

import com.tangzc.autotable.core.AutoTableBootstrap;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.strategy.mysql.mapper.MysqlTablesMapper;
import com.tangzc.autotable.core.strategy.pgsql.mapper.PgsqlTablesMapper;
import com.tangzc.autotable.core.strategy.sqlite.mapper.SqliteTablesMapper;
import com.tangzc.autotable.spring.boot.properties.AutoTableProperties;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
@Conditional(ProfileCondition.class)
@AutoConfigureAfter({DataSourceAutoConfiguration.class, MybatisAutoConfiguration.class})
public class AutoTableAutoConfig {

    private final SqlSessionTemplate sqlSessionTemplate;
    private final AutoTableProperties autoTableProperties;

    public AutoTableAutoConfig(SqlSessionTemplate sqlSessionTemplate, AutoTableProperties autoTableProperties) {
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.autoTableProperties = autoTableProperties;
    }

    @PostConstruct
    public void init() {

        // 设置全局的配置
        AutoTableGlobalConfig.setAutoTableProperties(autoTableProperties.toConfig());
        // 设置全局的SqlSessionFactory
        AutoTableGlobalConfig.setSqlSessionFactory(getSqlSessionFactory());
        // 启动AutoTable
        AutoTableBootstrap.start();
    }

    /**
     * 构建SqlSessionFactory
     */
    private SqlSessionFactory getSqlSessionFactory() {
        SqlSessionFactory sqlSessionFactory = sqlSessionTemplate.getSqlSessionFactory();
        org.apache.ibatis.session.Configuration configuration = sqlSessionFactory.getConfiguration();
        // 把内置的Mapper注入Mybatis
        configuration.addMapper(MysqlTablesMapper.class);
        configuration.addMapper(PgsqlTablesMapper.class);
        configuration.addMapper(SqliteTablesMapper.class);
        return sqlSessionFactory;
    }
}
