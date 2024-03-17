package com.tangzc.autotable.core.dynamicds;

import lombok.NonNull;
import org.apache.ibatis.session.SqlSessionFactory;

public class SqlSessionFactoryManager {

    /**
     * 当前数据源
     */
    private static final ThreadLocal<SqlSessionFactory> sqlSessionFactory = new ThreadLocal<>();

    public static void setSqlSessionFactory(@NonNull SqlSessionFactory sqlSessionFactory) {
        SqlSessionFactoryManager.sqlSessionFactory.set(sqlSessionFactory);
    }

    public static SqlSessionFactory getSqlSessionFactory() {
        SqlSessionFactory sessionFactory = sqlSessionFactory.get();
        if (sessionFactory == null) {
            throw new RuntimeException("当前数据源下，未找到对应的SqlSessionFactory");
        }
        return sessionFactory;
    }

    public static void cleanSqlSessionFactory() {
        sqlSessionFactory.remove();
    }
}
