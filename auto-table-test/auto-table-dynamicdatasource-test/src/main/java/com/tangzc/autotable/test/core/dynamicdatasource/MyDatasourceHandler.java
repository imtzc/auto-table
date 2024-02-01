package com.tangzc.autotable.test.core.dynamicdatasource;

import com.tangzc.autotable.core.dynamicds.IDatasourceHandler;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MyDatasourceHandler implements IDatasourceHandler<String> {

    private static final Map<String, SqlSessionFactory> STRING_SQL_SESSION_FACTORY_MAP = new HashMap<>();

    @Override
    public SqlSessionFactory useDataSource(String dataSource) {

        return STRING_SQL_SESSION_FACTORY_MAP.computeIfAbsent(dataSource, $ -> {

            String resource = "mybatis-config.xml";
            if (dataSource.equals("test")) {
                resource = "mybatis-config2.xml";
            }

            try (InputStream inputStream = TestApplication.class.getClassLoader().getResourceAsStream(resource)) {
                // 使用SqlSessionFactoryBuilder加载配置文件
                return new SqlSessionFactoryBuilder().build(inputStream);
            } catch (Exception e) {
                return null;
            }
        });
    }

    @Override
    public void clearDataSource(String dataSource) {

    }

    @Override
    public String getDataSource(Class<?> clazz) {
        Ds annotation = clazz.getAnnotation(Ds.class);
        if (annotation != null) {
            return annotation.value();
        }
        return "";
    }
}
