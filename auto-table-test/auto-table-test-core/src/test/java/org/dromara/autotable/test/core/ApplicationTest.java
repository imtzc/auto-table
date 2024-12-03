package org.dromara.autotable.test.core;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.dromara.autotable.core.AutoTableBootstrap;
import org.dromara.autotable.core.AutoTableGlobalConfig;
import org.dromara.autotable.core.RunMode;
import org.dromara.autotable.core.config.PropertyConfig;
import org.dromara.autotable.core.constants.Version;
import org.dromara.autotable.core.dynamicds.SqlSessionFactoryManager;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class ApplicationTest {

    @Before
    public void init() {

        initSqlSessionFactory("mybatis-config.xml");

        // 配置信息
        PropertyConfig autoTableProperties = new PropertyConfig();
        // create模式
        autoTableProperties.setMode(RunMode.create);
        // 指定扫描包
        // autoTableProperties.setModelPackage(new String[]{"org.dromara.**"});
        // 开启 删除不存在的列
        autoTableProperties.setAutoDropColumn(true);
        // 父类字段加到子类的前面
        autoTableProperties.setSuperInsertPosition(PropertyConfig.SuperInsertPosition.after);
        // 记录sql
        PropertyConfig.RecordSqlProperties recordSqlProperties = new PropertyConfig.RecordSqlProperties();
        recordSqlProperties.setEnable(true);
        recordSqlProperties.setVersion(Version.VALUE);
        autoTableProperties.setRecordSql(recordSqlProperties);

        // 自定义，以Flyway的格式记录sql
        recordSqlProperties.setRecordType(PropertyConfig.RecordSqlProperties.TypeEnum.custom);
        AutoTableGlobalConfig.setCustomRecordSqlHandler(new RecordSqlFlywayHandler("/Users/don/Downloads/sqlLogs"));

        // 以文件的形式，记录sql
        // recordSqlProperties.setRecordType(PropertyConfig.RecordSqlProperties.TypeEnum.file);
        // 指定记录方式为PropertyConfig.RecordSqlProperties.TypeEnum.file的情况下的文件路径
        // recordSqlProperties.setFolderPath("/Users/don/Downloads/sqlLogs");

        AutoTableGlobalConfig.setAutoTableProperties(autoTableProperties);
    }

    @Test
    public void testSqliteCreate() {

        initSqlSessionFactory("mybatis-config-sqlite.xml");

        AutoTableGlobalConfig.getAutoTableProperties().setMode(RunMode.create);
        AutoTableGlobalConfig.getAutoTableProperties().setModelClass(new Class[]{TestSqliteEntity.class});
        // 开始
        AutoTableBootstrap.start();
    }

    @Test
    public void testSqliteModify() {

        initSqlSessionFactory("mybatis-config-sqlite.xml");

        AutoTableGlobalConfig.getAutoTableProperties().setMode(RunMode.update);
        AutoTableGlobalConfig.getAutoTableProperties().setModelClass(new Class[]{TestSqliteEntity_.class});
        // 开始
        AutoTableBootstrap.start();
    }


    private void initSqlSessionFactory(String resource) {
        try (InputStream inputStream = ApplicationTest.class.getClassLoader().getResourceAsStream(resource)) {
            // 使用SqlSessionFactoryBuilder加载配置文件
            SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            // 设置当前数据源
            SqlSessionFactoryManager.setSqlSessionFactory(sessionFactory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
