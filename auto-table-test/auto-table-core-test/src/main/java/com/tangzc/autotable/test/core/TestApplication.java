package com.tangzc.autotable.test.core;

import com.tangzc.autotable.core.AutoTableBootstrap;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.constants.DatabaseDialect;
import com.tangzc.autotable.core.dynamicds.SqlSessionFactoryManager;
import com.tangzc.autotable.core.strategy.mysql.data.MysqlTableMetadata;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * 正常测试
 */
public class TestApplication {

    public static void main(String[] args) throws IOException {

        SqlSessionFactory sessionFactory;
        String resource = "mybatis-config.xml";
        try (InputStream inputStream = TestApplication.class.getClassLoader().getResourceAsStream(resource)) {
            // 使用SqlSessionFactoryBuilder加载配置文件
            sessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        }

        // 设置当前数据源
        SqlSessionFactoryManager.setSqlSessionFactory(sessionFactory);

        // 配置信息
        AutoTableGlobalConfig.PropertyConfig autoTableProperties = new AutoTableGlobalConfig.PropertyConfig();
        // 开启 删除不存在的列
        autoTableProperties.setAutoDropColumn(true);
        AutoTableGlobalConfig.setAutoTableProperties(autoTableProperties);

        // 修改表注释
        AutoTableGlobalConfig.setBuildTableMetadataIntercepter((databaseDialect, tableMetadata) -> {
            if (DatabaseDialect.MySQL.equals(databaseDialect)) {
                MysqlTableMetadata mysqlTableMetadata = (MysqlTableMetadata) tableMetadata;
                mysqlTableMetadata.setComment(mysqlTableMetadata.getComment() + "-我是表注释的小尾巴～");
            }
        });

        // 开始
        AutoTableBootstrap.start();
    }
}
