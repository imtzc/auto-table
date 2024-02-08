package com.tangzc.autotable.core;

import com.tangzc.autotable.core.dynamicds.IDataSourceHandler;
import com.tangzc.autotable.core.dynamicds.impl.DefaultDataSourceHandler;
import com.tangzc.autotable.core.intercepter.BuildTableMetadataIntercepter;
import com.tangzc.autotable.core.strategy.CompareTableInfo;
import com.tangzc.autotable.core.strategy.IStrategy;
import com.tangzc.autotable.core.strategy.TableMetadata;
import com.tangzc.autotable.core.strategy.mysql.JavaToMysqlConverter;
import com.tangzc.autotable.core.strategy.pgsql.JavaToPgsqlConverter;
import com.tangzc.autotable.core.strategy.sqlite.JavaToSqliteConverter;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class AutoTableGlobalConfig {

    @Setter
    @Getter
    private static PropertyConfig autoTableProperties = new PropertyConfig();

    @Setter
    @Getter
    private static IDataSourceHandler<?> datasourceHandler = new DefaultDataSourceHandler();

    @Setter
    @Getter
    private static AutoTableAnnotationFinder autoTableAnnotationFinder = new AutoTableAnnotationFinder(){};

    @Setter
    @Getter
    private static AutoTableOrmFrameAdapter autoTableOrmFrameAdapter = new AutoTableOrmFrameAdapter.DefaultAutoTableOrmFrameAdapter();

    @Setter
    @Getter
    private static JavaToMysqlConverter javaToMysqlConverter = new JavaToMysqlConverter(){};

    @Setter
    @Getter
    private static JavaToPgsqlConverter javaToPgsqlConverter = new JavaToPgsqlConverter(){};

    @Setter
    @Getter
    private static JavaToSqliteConverter javaToSqliteConverter = new JavaToSqliteConverter() {};

    @Setter
    @Getter
    private static BuildTableMetadataIntercepter buildTableMetadataIntercepter = (databaseDialect, tableMetadata) -> {
    };

    private final static Map<String, IStrategy<? extends TableMetadata, ? extends CompareTableInfo, ?>> strategyMap = new HashMap<>();

    public static void addStrategy(IStrategy<? extends TableMetadata, ? extends CompareTableInfo, ?> strategy) {
        strategyMap.put(strategy.databaseDialect(), strategy);
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
         * add：系统启动后，只做新增，比如新增表/新增字段/新增索引/新增唯一约束的功能，而不会去做修改和删除的操作。
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

        /**
         * 记录执行的SQL
         */
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
