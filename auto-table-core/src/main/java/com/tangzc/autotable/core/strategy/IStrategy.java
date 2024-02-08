package com.tangzc.autotable.core.strategy;

import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.RunMode;
import com.tangzc.autotable.core.dynamicds.SqlSessionFactoryManager;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author don
 */
public interface IStrategy<TABLE_META extends TableMetadata, COMPARE_TABLE_INFO extends CompareTableInfo, MAPPER> {

    Logger log = LoggerFactory.getLogger(IStrategy.class);

    /**
     * 策略对应的数据库方言，与数据库驱动中的接口{@link java.sql.DatabaseMetaData#getDatabaseProductName()}实现返回值一致
     * @return 方言
     */
    String databaseDialect();

    default void execute(Consumer<MAPPER> execute) {

        // 从接口泛型上读取MapperClass
        Class<MAPPER> mapperClass = getMapperClass();

        // 执行
        try (SqlSession sqlSession = SqlSessionFactoryManager.getSqlSessionFactory().openSession()) {
            execute.accept(sqlSession.getMapper(mapperClass));
        }
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
     * 分析bean class
     *
     * @param beanClasses 待处理的类
     */
    default void analyseClasses(Set<Class<?>> beanClasses) {

        AutoTableGlobalConfig.PropertyConfig autoTableProperties = AutoTableGlobalConfig.getAutoTableProperties();

        RunMode runMode = autoTableProperties.getMode();
        switch (runMode) {
            case none:
                break;
            case validate:
                validateMode(beanClasses);
                break;
            case add:
            case create:
            case update:
                addOrCreateOrUpdateMode(beanClasses, runMode == RunMode.create, runMode == RunMode.update);
                break;
        }
    }

    default void addOrCreateOrUpdateMode(Set<Class<?>> beanClasses, boolean isCreateMode, boolean isUpdateMode) {
        for (Class<?> beanClass : beanClasses) {

            TABLE_META tableMetadata = this.analyseClass(beanClass);
            // 拦截表信息，供用户自定义修改
            AutoTableGlobalConfig.getBuildTableMetadataIntercepter().intercept(this.databaseDialect(), tableMetadata);

            // 构建数据模型失败跳过
            if (tableMetadata == null) {
                continue;
            }

            String tableName = tableMetadata.getTableName();

            // 表是否存在的标记
            boolean tableIsExist;
            if (isCreateMode) {
                log.info("create模式，删除表：{}", tableName);
                // 直接删除表重新生成
                this.dropTable(tableName);
                // 上一步表被删了，肯定不存在
                tableIsExist = false;
            } else {
                tableIsExist = this.checkTableExist(tableName);
            }

            // 当表不存在的时候，直接生成表
            if (!tableIsExist) {
                log.info("创建表：{}", tableName);
                // 建表
                this.createTable(tableMetadata);
            }

            // update模型，且表存在，更新表信息
            if (isUpdateMode && tableIsExist) {
                // 当表存在，比对表与Bean描述的差异
                COMPARE_TABLE_INFO compareTableInfo = this.compareTable(tableMetadata);
                if (compareTableInfo.needModify()) {
                    log.info("修改表：{}", tableName);
                    // 修改表信息
                    this.modifyTable(compareTableInfo);
                }
            }
        }
    }

    default void validateMode(Set<Class<?>> beanClasses) {
        List<String> validateResult = new ArrayList<>();
        for (Class<?> beanClass : beanClasses) {

            TABLE_META tableMetadata = this.analyseClass(beanClass);

            // 构建数据模型失败跳过
            if (tableMetadata == null) {
                continue;
            }

            String tableName = tableMetadata.getTableName();
            // 检查数据库数据模型与实体是否一致
            boolean tableIsExist = this.checkTableExist(tableName);
            if (tableIsExist) {
                COMPARE_TABLE_INFO compareTableInfo = this.compareTable(tableMetadata);
                if (compareTableInfo.needModify()) {
                    validateResult.add("表" + tableName + "结构不一致");
                }
            } else {
                validateResult.add("表" + tableName + "不存在");
            }
        }
        if (!validateResult.isEmpty()) {
            throw new RuntimeException("启动失败，" + this.databaseDialect() + "数据库与实体模型不对应：\n" + String.join("\n", validateResult));
        }
    }

    /**
     * 根据表名删除表
     *
     * @param tableName 表名
     */
    void dropTable(String tableName);

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
    TABLE_META analyseClass(Class<?> beanClass);

    /**
     * 创建表
     *
     * @param tableMetadata 表元数据
     */
    void createTable(TABLE_META tableMetadata);

    /**
     * 对比表与bean的差异
     *
     * @param tableMetadata 表元数据
     * @return 待修改的表信息描述
     */
    COMPARE_TABLE_INFO compareTable(TABLE_META tableMetadata);

    /**
     * 修改表
     *
     * @param compareTableInfo 修改表的描述信息
     */
    void modifyTable(COMPARE_TABLE_INFO compareTableInfo);
}
