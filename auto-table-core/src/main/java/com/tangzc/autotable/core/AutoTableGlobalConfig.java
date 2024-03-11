package com.tangzc.autotable.core;

import com.tangzc.autotable.core.callback.CreateTableFinishCallback;
import com.tangzc.autotable.core.callback.ModifyTableFinishCallback;
import com.tangzc.autotable.core.callback.RunStateCallback;
import com.tangzc.autotable.core.callback.ValidateFinishCallback;
import com.tangzc.autotable.core.converter.JavaTypeToDatabaseTypeConverter;
import com.tangzc.autotable.core.dynamicds.IDataSourceHandler;
import com.tangzc.autotable.core.dynamicds.impl.DefaultDataSourceHandler;
import com.tangzc.autotable.core.intercepter.AutoTableAnnotationIntercepter;
import com.tangzc.autotable.core.intercepter.BuildTableMetadataIntercepter;
import com.tangzc.autotable.core.intercepter.CreateTableIntercepter;
import com.tangzc.autotable.core.intercepter.ModifyTableIntercepter;
import com.tangzc.autotable.core.strategy.CompareTableInfo;
import com.tangzc.autotable.core.strategy.IStrategy;
import com.tangzc.autotable.core.strategy.TableMetadata;
import lombok.Data;
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
    private static AutoTableAnnotationIntercepter autoTableAnnotationIntercepter = (includeAnnotations, excludeAnnotations) -> {
    };

    /**
     * 创建表拦截
     */
    @Setter
    @Getter
    private static BuildTableMetadataIntercepter buildTableMetadataIntercepter = (databaseDialect, tableMetadata) -> {
    };

    /**
     * 创建表拦截
     */
    @Setter
    @Getter
    private static CreateTableIntercepter createTableIntercepter = (databaseDialect, tableMetadata) -> {
    };

    /**
     * 修改表拦截
     */
    @Setter
    @Getter
    private static ModifyTableIntercepter modifyTableIntercepter = (databaseDialect, tableMetadata, compareTableInfo) -> {
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

    @Data
    public static class PropertyConfig {

        /**
         * 是否显示banner
         */
        private Boolean showBanner = true;
        /**
         * 是否启用自动维护表功能
         */
        private Boolean enable = true;
        /**
         * 启动模式
         * none：系统不做任何处理。
         * create：系统启动后，会先将所有的表删除掉，然后根据model中配置的结构重新建表，该操作会破坏原有数据。
         * update：系统启动后，会自动判断哪些表是新建的，哪些字段要新增修改，哪些索引/约束要新增删除等，该操作不会删除字段(更改字段名称的情况下，会认为是新增字段)。
         */
        private RunMode mode = RunMode.update;
        /**
         * 您的model包路径，多个路径可以用分号或者逗号隔开，会递归这个目录下的全部目录中的java对象，支持类似com.bz.**.entity
         * 缺省值：[Spring启动类所在包]
         */
        private String[] modelPackage;
        /**
         * 自己定义的索引前缀
         */
        private String indexPrefix = "auto_idx_";
        /**
         * 自动删除名称不匹配的字段：强烈不建议开启，会发生丢失数据等不可逆的操作。
         */
        private Boolean autoDropColumn = false;
        /**
         * 是否自动删除名称不匹配的索引
         */
        private Boolean autoDropIndex = true;

        /**
         * mysql配置
         */
        private MysqlConfig mysql = new MysqlConfig();

        // /**
        //  * 记录执行的SQL
        //  */
        // private RecordSqlProperties recordSql = new RecordSqlProperties();
    }

    @Data
    public static class MysqlConfig {
        /**
         * 表默认字符集
         */
        private String tableDefaultCharset = "utf8mb4";
        /**
         * 表默认排序规则
         */
        private String tableDefaultCollation = "utf8mb4_0900_ai_ci";
        /**
         * 列默认字符集
         */
        private String columnDefaultCharset = "utf8mb4";
        /**
         * 列默认排序规则
         */
        private String columnDefaultCollation = "utf8mb4_0900_ai_ci";
    }
}
