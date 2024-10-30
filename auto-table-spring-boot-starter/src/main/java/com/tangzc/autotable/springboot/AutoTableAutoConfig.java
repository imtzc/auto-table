package com.tangzc.autotable.springboot;

import com.tangzc.autotable.core.AutoTableAnnotationFinder;
import com.tangzc.autotable.core.AutoTableBootstrap;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.AutoTableOrmFrameAdapter;
import com.tangzc.autotable.core.callback.CreateTableFinishCallback;
import com.tangzc.autotable.core.callback.ModifyTableFinishCallback;
import com.tangzc.autotable.core.callback.RunStateCallback;
import com.tangzc.autotable.core.callback.ValidateFinishCallback;
import com.tangzc.autotable.core.converter.JavaTypeToDatabaseTypeConverter;
import com.tangzc.autotable.core.dynamicds.IDataSourceHandler;
import com.tangzc.autotable.core.dynamicds.SqlSessionFactoryManager;
import com.tangzc.autotable.core.interceptor.AutoTableAnnotationInterceptor;
import com.tangzc.autotable.core.interceptor.BuildTableMetadataInterceptor;
import com.tangzc.autotable.core.interceptor.CreateTableInterceptor;
import com.tangzc.autotable.core.interceptor.ModifyTableInterceptor;
import com.tangzc.autotable.core.recordsql.RecordSqlHandler;
import com.tangzc.autotable.core.strategy.CompareTableInfo;
import com.tangzc.autotable.core.strategy.IStrategy;
import com.tangzc.autotable.core.strategy.TableMetadata;
import com.tangzc.autotable.springboot.properties.AutoTableProperties;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

/**
 * @author don
 */
@Configuration
@AutoConfigureAfter({DataSourceAutoConfiguration.class})
public class AutoTableAutoConfig {

    public AutoTableAutoConfig(
            SqlSessionTemplate sqlSessionTemplate,
            AutoTableProperties autoTableProperties,
            ObjectProvider<IStrategy<? extends TableMetadata, ? extends CompareTableInfo, ?>> strategies,
            ObjectProvider<AutoTableAnnotationFinder> autoTableAnnotationFinder,
            ObjectProvider<AutoTableOrmFrameAdapter> autoTableOrmFrameAdapter,
            ObjectProvider<IDataSourceHandler> dynamicDataSourceHandler,
            ObjectProvider<RecordSqlHandler> recordSqlHandler,
            /* 拦截器 */
            ObjectProvider<AutoTableAnnotationInterceptor> autoTableAnnotationInterceptor,
            ObjectProvider<BuildTableMetadataInterceptor> buildTableMetadataInterceptor,
            ObjectProvider<CreateTableInterceptor> createTableInterceptor,
            ObjectProvider<ModifyTableInterceptor> modifyTableInterceptor,
            /* 回调事件 */
            ObjectProvider<CreateTableFinishCallback> createTableFinishCallback,
            ObjectProvider<ModifyTableFinishCallback> modifyTableFinishCallback,
            ObjectProvider<RunStateCallback> runStateCallback,
            ObjectProvider<ValidateFinishCallback> validateFinishCallback,

            ObjectProvider<JavaTypeToDatabaseTypeConverter> javaTypeToDatabaseTypeConverter) {

        // 默认设置全局的SqlSessionFactory
        SqlSessionFactoryManager.setSqlSessionFactory(sqlSessionTemplate.getSqlSessionFactory());

        // 设置全局的配置
        AutoTableGlobalConfig.setAutoTableProperties(autoTableProperties.toConfig());

        // 假如有自定的注解扫描器，就使用自定义的注解扫描器。没有，则设置内置的注解扫描器
        AutoTableGlobalConfig.setAutoTableAnnotationFinder(autoTableAnnotationFinder.getIfAvailable(CustomAnnotationFinder::new));

        // 如果有自定义的数据库策略，则加载
        strategies.stream().forEach(AutoTableGlobalConfig::addStrategy);

        // 假如有自定义的orm框架适配器，就使用自定义的orm框架适配器
        autoTableOrmFrameAdapter.ifAvailable(AutoTableGlobalConfig::setAutoTableOrmFrameAdapter);

        // 假如有自定义的动态数据源处理器，就使用自定义的动态数据源处理器
        dynamicDataSourceHandler.ifAvailable(AutoTableGlobalConfig::setDatasourceHandler);

        // 假如有自定义的SQL记录处理器，就使用自定义的SQL记录处理器
        recordSqlHandler.ifAvailable(AutoTableGlobalConfig::setCustomRecordSqlHandler);

        /* 拦截器 */
        // 假如有自定义的注解拦截器，就使用自定义的注解拦截器
        autoTableAnnotationInterceptor.ifAvailable(AutoTableGlobalConfig::setAutoTableAnnotationInterceptor);
        // 假如有自定义的创建表拦截器，就使用自定义的创建表拦截器
        buildTableMetadataInterceptor.ifAvailable(AutoTableGlobalConfig::setBuildTableMetadataInterceptor);
        // 假如有自定义的创建表拦截器，就使用自定义的创建表拦截器
        createTableInterceptor.ifAvailable(AutoTableGlobalConfig::setCreateTableInterceptor);
        // 假如有自定义的修改表拦截器，就使用自定义的修改表拦截器
        modifyTableInterceptor.ifAvailable(AutoTableGlobalConfig::setModifyTableInterceptor);

        /* 回调事件 */
        // 假如有自定义的创建表回调，就使用自定义的创建表回调
        createTableFinishCallback.ifAvailable(AutoTableGlobalConfig::setCreateTableFinishCallback);
        // 假如有自定义的修改表回调，就使用自定义的修改表回调
        modifyTableFinishCallback.ifAvailable(AutoTableGlobalConfig::setModifyTableFinishCallback);
        // 假如有自定义的单个表执行前后回调，就使用自定义的单个表执行前后回调
        runStateCallback.ifAvailable(AutoTableGlobalConfig::setRunStateCallback);
        // 假如有自定义的验证表回调，就使用自定义的验证表回调
        validateFinishCallback.ifAvailable(AutoTableGlobalConfig::setValidateFinishCallback);

        // 假如有自定义的java到数据库的转换器，就使用自定义的java到数据库的转换器
        javaTypeToDatabaseTypeConverter.ifAvailable(AutoTableGlobalConfig::setJavaTypeToDatabaseTypeConverter);
    }

    @EventListener(ContextRefreshedEvent.class)
    public void run() {

        // 启动AutoTable
        if(!isTestEnvironment()) {
            AutoTableBootstrap.start();
        }
    }

    public boolean isTestEnvironment() {
        try {
            // 尝试加载JUnit测试类
            Class.forName("org.junit.jupiter.api.Test");
            return true; // 如果找到则表示在测试环境中
        } catch (ClassNotFoundException e) {
            return false; // 否则是正常启动环境
        }
    }
}
