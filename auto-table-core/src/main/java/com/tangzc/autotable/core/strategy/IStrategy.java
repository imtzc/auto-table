package com.tangzc.autotable.core.strategy;

import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.RunMode;
import com.tangzc.autotable.core.converter.DefaultTypeEnumInterface;
import com.tangzc.autotable.core.dynamicds.SqlSessionFactoryManager;
import lombok.NonNull;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author don
 */
public interface IStrategy<TABLE_META extends TableMetadata, COMPARE_TABLE_INFO extends CompareTableInfo, MAPPER> {

    Logger log = LoggerFactory.getLogger(IStrategy.class);

    default void execute(Consumer<MAPPER> execute) {
        executeReturn(mapper -> {
            execute.accept(mapper);
            return null;
        });
    }

    default <R> R executeReturn(Function<MAPPER, R> execute) {

        // 从接口泛型上读取MapperClass
        Class<MAPPER> mapperClass = getMapperClass();

        // 执行
        try (SqlSession sqlSession = SqlSessionFactoryManager.getSqlSessionFactory().openSession()) {
            return execute.apply(sqlSession.getMapper(mapperClass));
        }
    }

    /**
     * 从接口泛型上读取MapperClass
     *
     * @return MapperClass
     */
    default Class<MAPPER> getMapperClass() {

        // 从接口泛型上读取MapperClass
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericInterfaces()[0];
        Class<MAPPER> mapperClass = (Class<MAPPER>) genericSuperclass.getActualTypeArguments()[2];

        // 如果没有注册Mapper，则注册
        SqlSessionFactory sqlSessionFactory = SqlSessionFactoryManager.getSqlSessionFactory();
        Configuration configuration = sqlSessionFactory.getConfiguration();
        if (!configuration.hasMapper(mapperClass)) {
            configuration.addMapper(mapperClass);
        }

        return mapperClass;
    }

    /**
     * 开始分析实体集合
     *
     * @param entityClass 待处理的实体
     */
    default void start(Class<?> entityClass) {

        AutoTableGlobalConfig.getRunStateCallback().before(entityClass);

        TABLE_META tableMetadata = this.analyseClass(entityClass);

        start(tableMetadata);

        AutoTableGlobalConfig.getRunStateCallback().after(entityClass);
    }

    /**
     * 开始分析实体
     *
     * @param tableMetadata 表元数据
     */
    default void start(TABLE_META tableMetadata) {
        // 拦截表信息，供用户自定义修改
        AutoTableGlobalConfig.getBuildTableMetadataInterceptor().intercept(this.databaseDialect(), tableMetadata);

        RunMode runMode = AutoTableGlobalConfig.getAutoTableProperties().getMode();

        switch (runMode) {
            case validate:
                validateMode(tableMetadata);
                break;
            case create:
                createMode(tableMetadata);
                break;
            case update:
                updateMode(tableMetadata);
                break;
            default:
                throw new RuntimeException(String.format("不支持的运行模式：%s", runMode));
        }
    }

    /**
     * 检查数据库数据模型与实体是否一致
     * 1. 检查数据库数据模型是否存在
     * 2. 检查数据库数据模型与实体是否一致
     */
    default void validateMode(TABLE_META tableMetadata) {

        String tableName = tableMetadata.getTableName();

        // 检查数据库数据模型与实体是否一致
        boolean tableNotExist = !this.checkTableExist(tableName);
        if (tableNotExist) {
            AutoTableGlobalConfig.getValidateFinishCallback().validateFinish(false, this.databaseDialect(), null);
            throw new RuntimeException(String.format("启动失败，%s中不存在表%s", this.databaseDialect(), tableMetadata.getTableName()));
        }

        // 对比数据库表结构与新的表元数据的差异
        COMPARE_TABLE_INFO compareTableInfo = this.compareTable(tableMetadata);
        if (compareTableInfo.needModify()) {
            log.warn(compareTableInfo.validateFailedMessage());
            AutoTableGlobalConfig.getValidateFinishCallback().validateFinish(false, this.databaseDialect(), compareTableInfo);
            throw new RuntimeException("启动失败，" + this.databaseDialect() + "数据表" + tableMetadata.getTableName() + "与实体不匹配");
        }
        AutoTableGlobalConfig.getValidateFinishCallback().validateFinish(true, this.databaseDialect(), compareTableInfo);
    }

    /**
     * 创建模式
     * 1. 删除表
     * 2. 新建表
     */
    default void createMode(TABLE_META tableMetadata) {

        String tableName = tableMetadata.getTableName();

        // 表是否存在的标记
        log.info("create模式，删除表：{}", tableName);
        // 直接尝试删除表
        String sql = this.dropTable(tableName);
        executeSql(Collections.singletonList(sql));

        // 新建表
        executeCreateTable(tableMetadata);
    }

    /**
     * 更新模式
     * 1. 检查表是否存在
     * 2. 不存在创建
     * 3. 检查表是否需要修改
     * 4. 需要修改就修改表
     */
    default void updateMode(TABLE_META tableMetadata) {

        String tableName = tableMetadata.getTableName();

        boolean tableNotExist = !this.checkTableExist(tableName);
        // 当表不存在的时候，直接创建表
        if (tableNotExist) {
            executeCreateTable(tableMetadata);
            return;
        }

        // 当表存在，比对数据库表结构与表元数据的差异
        COMPARE_TABLE_INFO compareTableInfo = this.compareTable(tableMetadata);
        if (compareTableInfo.needModify()) {
            // 修改表信息
            log.info("修改表：{}", tableName);
            AutoTableGlobalConfig.getModifyTableInterceptor().beforeModifyTable(this.databaseDialect(), tableMetadata, compareTableInfo);
            List<String> sqlList = this.modifyTable(compareTableInfo);
            executeSql(sqlList);
            AutoTableGlobalConfig.getModifyTableFinishCallback().afterModifyTable(this.databaseDialect(), tableMetadata, compareTableInfo);
        }
    }

    default void executeCreateTable(TABLE_META tableMetadata) {

        String tableName = tableMetadata.getTableName();
        log.info("创建表：{}", tableName);

        AutoTableGlobalConfig.getCreateTableInterceptor().beforeCreateTable(this.databaseDialect(), tableMetadata);
        List<String> sqlList = this.createTable(tableMetadata);
        executeSql(sqlList);
        AutoTableGlobalConfig.getCreateTableFinishCallback().afterCreateTable(this.databaseDialect(), tableMetadata);
    }

    default void executeSql(List<String> sqlList) {
        SqlSessionFactory sqlSessionFactory = SqlSessionFactoryManager.getSqlSessionFactory();
        try (SqlSession sqlSession = sqlSessionFactory.openSession();
             Statement statement = sqlSession.getConnection().createStatement()) {
            for (String sql : sqlList) {
                log.info("执行sql：{}", sql);
                statement.execute(sql);
            }
            // 提交事务（如果开启了自动提交，则这步可以省略）
            sqlSession.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 策略对应的数据库方言，与数据库驱动中的接口{@link java.sql.DatabaseMetaData#getDatabaseProductName()}实现返回值一致
     *
     * @return 方言
     */
    String databaseDialect();

    /**
     * java字段类型与数据库类型映射关系
     *
     * @return 映射
     */
    Map<Class<?>, DefaultTypeEnumInterface> typeMapping();

    /**
     * 根据表名删除表
     *
     * @param tableName 表名
     */
    String dropTable(String tableName);

    /**
     * 检查表是否存在
     *
     * @param tableName 表名
     * @return 表详情
     */
    boolean checkTableExist(String tableName);

    /**
     * 分析Bean，得到元数据信息
     *
     * @param beanClass 待分析的class
     * @return 表元信息
     */
    @NonNull TABLE_META analyseClass(Class<?> beanClass);

    /**
     * 创建表
     *
     * @param tableMetadata 表元数据
     */
    List<String> createTable(TABLE_META tableMetadata);

    /**
     * 对比表与bean的差异
     *
     * @param tableMetadata 表元数据
     * @return 待修改的表信息描述
     */
    @NonNull COMPARE_TABLE_INFO compareTable(TABLE_META tableMetadata);

    /**
     * 修改表
     *
     * @param compareTableInfo 修改表的描述信息
     */
    List<String> modifyTable(COMPARE_TABLE_INFO compareTableInfo);
}
