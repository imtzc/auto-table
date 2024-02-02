package com.tangzc.autotable.test.springboot.dynamicdatasource.dynamicdatasourceframe;

import java.util.Stack;

public class DynamicDataSourceContextHolder {

    /**
     * 动态数据源名称上下文
     */
    private static final ThreadLocal<Stack<String>> DATASOURCE_CONTEXT_KEY_HOLDER = ThreadLocal.withInitial(Stack::new);

    public static void setContextKey(String dataSourceName) {
        DATASOURCE_CONTEXT_KEY_HOLDER.get().push(dataSourceName);
    }

    public static void removeContextKey() {
        DATASOURCE_CONTEXT_KEY_HOLDER.get().pop();
    }

    public static String getContextKey() {
        return DATASOURCE_CONTEXT_KEY_HOLDER.get().peek();
    }
}
