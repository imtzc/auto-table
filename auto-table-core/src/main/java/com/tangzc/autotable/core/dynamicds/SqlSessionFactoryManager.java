package com.tangzc.autotable.core.dynamicds;

import lombok.NonNull;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * @author don
 */
public class SqlSessionFactoryManager {

    /**
     * 当前数据源
     */
    private static final ThreadLocal<SqlSessionFactory> SQL_SESSION_FACTORY = new ThreadLocal<>();

    public static void setSqlSessionFactory(@NonNull SqlSessionFactory sqlSessionFactory) {
        SqlSessionFactoryManager.SQL_SESSION_FACTORY.set(sqlSessionFactory);
    }

    public static SqlSessionFactory getSqlSessionFactory() {
        SqlSessionFactory sessionFactory = SQL_SESSION_FACTORY.get();
        if (sessionFactory == null) {
            throw new RuntimeException("当前数据源下，未找到对应的SqlSessionFactory");
        }
        return sessionFactory;
    }

    public static void cleanSqlSessionFactory() {
        SQL_SESSION_FACTORY.remove();
    }
}
