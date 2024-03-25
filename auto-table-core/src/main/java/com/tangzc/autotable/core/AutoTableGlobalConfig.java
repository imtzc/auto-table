package com.tangzc.autotable.core;

import com.tangzc.autotable.core.callback.CreateTableFinishCallback;
import com.tangzc.autotable.core.callback.ModifyTableFinishCallback;
import com.tangzc.autotable.core.callback.RunStateCallback;
import com.tangzc.autotable.core.callback.ValidateFinishCallback;
import com.tangzc.autotable.core.config.PropertyConfig;
import com.tangzc.autotable.core.converter.JavaTypeToDatabaseTypeConverter;
import com.tangzc.autotable.core.dynamicds.IDataSourceHandler;
import com.tangzc.autotable.core.dynamicds.impl.DefaultDataSourceHandler;
import com.tangzc.autotable.core.interceptor.AutoTableAnnotationInterceptor;
import com.tangzc.autotable.core.interceptor.BuildTableMetadataInterceptor;
import com.tangzc.autotable.core.interceptor.CreateTableInterceptor;
import com.tangzc.autotable.core.interceptor.ModifyTableInterceptor;
import com.tangzc.autotable.core.strategy.CompareTableInfo;
import com.tangzc.autotable.core.strategy.IStrategy;
import com.tangzc.autotable.core.strategy.TableMetadata;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class AutoTableGlobalConfig {

    /**
     * 全局配置
     */
    @Setter
    @Getter
    private static PropertyConfig autoTableProperties = new PropertyConfig();

    /**
     * 数据源处理器
     */
    @Setter
    @Getter
    private static IDataSourceHandler<?> datasourceHandler = new DefaultDataSourceHandler();

    /**
     * 自定义注解查找器
     */
    @Setter
    @Getter
    private static AutoTableAnnotationFinder autoTableAnnotationFinder = new AutoTableAnnotationFinder() {
    };

    /**
     * ORM框架适配器
     */
    @Setter
    @Getter
    private static AutoTableOrmFrameAdapter autoTableOrmFrameAdapter = new AutoTableOrmFrameAdapter.DefaultAutoTableOrmFrameAdapter();

    /**
     * 数据库类型转换
     */
    @Setter
    @Getter
    private static JavaTypeToDatabaseTypeConverter javaTypeToDatabaseTypeConverter = new JavaTypeToDatabaseTypeConverter() {
    };

    /**
     * 自动表注解拦截器
     */
    @Setter
    @Getter
    private static AutoTableAnnotationInterceptor autoTableAnnotationInterceptor = (includeAnnotations, excludeAnnotations) -> {
    };

    /**
     * 创建表拦截
     */
    @Setter
    @Getter
    private static BuildTableMetadataInterceptor buildTableMetadataInterceptor = (databaseDialect, tableMetadata) -> {
    };

    /**
     * 创建表拦截
     */
    @Setter
    @Getter
    private static CreateTableInterceptor createTableInterceptor = (databaseDialect, tableMetadata) -> {
    };

    /**
     * 修改表拦截
     */
    @Setter
    @Getter
    private static ModifyTableInterceptor modifyTableInterceptor = (databaseDialect, tableMetadata, compareTableInfo) -> {
    };

    /**
     * 验证完成回调
     */
    @Setter
    @Getter
    private static ValidateFinishCallback validateFinishCallback = (status, databaseDialect, compareTableInfo) -> {
    };

    /**
     * 创建表回调
     */
    @Setter
    @Getter
    private static CreateTableFinishCallback createTableFinishCallback = (databaseDialect, tableMetadata) -> {
    };

    /**
     * 修改表回调
     */
    @Setter
    @Getter
    private static ModifyTableFinishCallback modifyTableFinishCallback = (databaseDialect, tableMetadata, compareTableInfo) -> {

    };
    /**
     * 单个表执行前后回调
     */
    @Setter
    @Getter
    private static RunStateCallback runStateCallback = new RunStateCallback() {
        @Override
        public void before(Class<?> tableClass) {
        }

        @Override
        public void after(Class<?> tableClass) {
        }
    };

    private final static Map<String, IStrategy<? extends TableMetadata, ? extends CompareTableInfo, ?>> strategyMap = new HashMap<>();

    public static void addStrategy(IStrategy<? extends TableMetadata, ? extends CompareTableInfo, ?> strategy) {
        strategyMap.put(strategy.databaseDialect(), strategy);
        JavaTypeToDatabaseTypeConverter.addTypeMapping(strategy.databaseDialect(), strategy.typeMapping());
    }

    public static IStrategy<?, ?, ?> getStrategy(String databaseDialect) {
        return strategyMap.get(databaseDialect);
    }

}
