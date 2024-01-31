package com.tangzc.autotable.core.strategy;

import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.constants.DatabaseDialect;
import com.tangzc.autotable.core.constants.RunMode;
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
     * 策略对应的数据库方言
     *
     * @return 方言
     */
    DatabaseDialect dbDialect();

    default void execute(Consumer<MAPPER> execute) {
        SqlSessionFactory sqlSessionFactory = AutoTableGlobalConfig.getSqlSessionFactory();
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericInterfaces()[0];
            execute.accept(sqlSession.getMapper((Class<MAPPER>) genericSuperclass.getActualTypeArguments()[2]));
        }
    }

    default <R> R executeRet(Function<MAPPER, R> execute) {
        SqlSessionFactory sqlSessionFactory = AutoTableGlobalConfig.getSqlSessionFactory();
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericInterfaces()[0];
            return execute.apply(sqlSession.getMapper((Class<MAPPER>) genericSuperclass.getActualTypeArguments()[2]));
        }
    }

    /**
     * 分析bean class
     *
     * @param beanClasses 待处理的类
     */
    default void analyseClasses(Set<Class<?>> beanClasses) {

        AutoTableGlobalConfig.PropertyConfig autoTableProperties = AutoTableGlobalConfig.getAutoTableProperties();

        if (autoTableProperties.getMode() == RunMode.validate) {
            validateMode(beanClasses);
        } else {
            createOrUpdateMode(beanClasses, autoTableProperties.getMode());
        }
    }

    default void createOrUpdateMode(Set<Class<?>> beanClasses, RunMode runMode) {
        for (Class<?> beanClass : beanClasses) {

            TABLE_META tableMetadata = this.analyseClass(beanClass);

            // 构建数据模型失败跳过
            if (tableMetadata == null) {
                continue;
            }

            String tableName = tableMetadata.getTableName();

            if (runMode == RunMode.create) {
                // create模式特殊对待，如果配置文件配置的是create，表示将所有的表删掉重新创建
                log.info("create模式，删除表：{}", tableName);
                // 直接删除表重新生成
                this.dropTable(tableName);
            }

            // 判断表是否存在
            boolean tableIsExist = this.checkTableExist(tableName);
            if (tableIsExist) {
                // 当表存在，比对表与Bean描述的差异
                COMPARE_TABLE_INFO compareTableInfo = this.compareTable(tableMetadata);
                if (compareTableInfo.needModify()) {
                    // 修改表信息
                    this.modifyTable(compareTableInfo);
                }
            } else {
                // 当表不存在的时候，直接生成表
                log.info("创建表：{}", tableName);
                this.createTable(tableMetadata);
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
            throw new RuntimeException("启动失败，" + this.dbDialect() + "数据库与实体模型不对应：\n" + String.join("\n", validateResult));
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
