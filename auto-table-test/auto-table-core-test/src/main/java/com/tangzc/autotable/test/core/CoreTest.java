package com.tangzc.autotable.test.core;

import com.tangzc.autotable.core.AutoTableBootstrap;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;

public class CoreTest {

    public static void main(String[] args) {
        String resource = "mybatis-config.xml";
        try (InputStream inputStream = CoreTest.class.getClassLoader().getResourceAsStream(resource)) {
            // 使用SqlSessionFactoryBuilder加载配置文件
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            AutoTableGlobalConfig.setSqlSessionFactory(sqlSessionFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }

        AutoTableBootstrap.start();
    }
}
