package com.tangzc.autotable.test.core;

import com.tangzc.autotable.core.AutoTableBootstrap;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.RunMode;
import com.tangzc.autotable.core.config.PropertyConfig;
import com.tangzc.autotable.core.constants.DatabaseDialect;
import com.tangzc.autotable.core.constants.Version;
import com.tangzc.autotable.core.dynamicds.SqlSessionFactoryManager;
import com.tangzc.autotable.core.strategy.mysql.data.MysqlTableMetadata;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * 正常测试
 * @author don
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
        PropertyConfig autoTableProperties = new PropertyConfig();
        // create模式
        autoTableProperties.setMode(RunMode.create);
        // 开启 删除不存在的列
        autoTableProperties.setAutoDropColumn(true);
        // 父类字段加到子类的前面
        autoTableProperties.setSuperInsertPosition(PropertyConfig.SuperInsertPosition.after);
        // 记录sql
        PropertyConfig.RecordSqlProperties recordSqlProperties = new PropertyConfig.RecordSqlProperties();
        recordSqlProperties.setEnable(true);
        recordSqlProperties.setRecordType(PropertyConfig.RecordSqlProperties.TypeEnum.file);
        // 指定记录方式为PropertyConfig.RecordSqlProperties.TypeEnum.file的情况下的文件路径
        recordSqlProperties.setFolderPath("/Users/don/Downloads/sqlLogs");
        recordSqlProperties.setVersion(Version.VALUE);
        autoTableProperties.setRecordSql(recordSqlProperties);

        AutoTableGlobalConfig.setAutoTableProperties(autoTableProperties);

        // 修改表信息
        AutoTableGlobalConfig.setBuildTableMetadataInterceptor((databaseDialect, tableMetadata) -> {

            // 修改表注释
            tableMetadata.setComment(tableMetadata.getComment() + "-我是表注释的小尾巴～");

            // 修改mysql特殊的表属性
            if (DatabaseDialect.MySQL.equals(databaseDialect)) {
                MysqlTableMetadata mysqlTableMetadata = (MysqlTableMetadata) tableMetadata;
                mysqlTableMetadata.setCharacterSet("utf8mb4");
                mysqlTableMetadata.setCollate("utf8mb4_0900_ai_ci");
            }
        });

        // 开始
        AutoTableBootstrap.start();
    }
}
