package com.tangzc.autotable.test.core.dynamicdatasource;

import com.tangzc.autotable.core.dynamicds.IDataSourceHandler;
import com.tangzc.autotable.core.dynamicds.SqlSessionFactoryManager;
import lombok.NonNull;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class DynamicDataSourceHandler implements IDataSourceHandler<String> {

    private static final Map<String, String> CONFIG_MAP = new HashMap<String, String>(){{
        put("mysql", "mybatis-config.xml");
        put("pgsql", "mybatis-config2.xml");
    }};
    private static final Map<String, SqlSessionFactory> STRING_SQL_SESSION_FACTORY_MAP = new HashMap<>();

    @Override
    public void useDataSource(String dataSourceName) {

        SqlSessionFactory sqlSessionFactory = STRING_SQL_SESSION_FACTORY_MAP.computeIfAbsent(dataSourceName, $ -> {

            String resource = CONFIG_MAP.get(dataSourceName);

            try (InputStream inputStream = TestApplication.class.getClassLoader().getResourceAsStream(resource)) {
                // 使用SqlSessionFactoryBuilder加载配置文件
                return new SqlSessionFactoryBuilder().build(inputStream);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // 设置新的SqlSessionFactory
        SqlSessionFactoryManager.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public void clearDataSource(String dataSourceName) {
    }

    @Override
    public @NonNull String getDataSourceName(Class<?> clazz) {
        Ds annotation = clazz.getAnnotation(Ds.class);
        if (annotation != null) {
            return annotation.value();
        }
        return "";
    }
}
