package com.tangzc.autotable.springboot;

import com.tangzc.autotable.core.AutoTableAnnotationFinder;
import com.tangzc.autotable.core.AutoTableBootstrap;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.AutoTableOrmFrameAdapter;
import com.tangzc.autotable.core.callback.CreateTableFinishCallback;
import com.tangzc.autotable.core.callback.ModifyTableFinishCallback;
import com.tangzc.autotable.core.callback.RunFinishCallback;
import com.tangzc.autotable.core.callback.ValidateFinishCallback;
import com.tangzc.autotable.core.converter.JavaTypeToDatabaseTypeConverter;
import com.tangzc.autotable.core.dynamicds.IDataSourceHandler;
import com.tangzc.autotable.core.dynamicds.SqlSessionFactoryManager;
import com.tangzc.autotable.core.intercepter.AutoTableAnnotationIntercepter;
import com.tangzc.autotable.core.intercepter.BuildTableMetadataIntercepter;
import com.tangzc.autotable.core.intercepter.CollectEntitiesIntercepter;
import com.tangzc.autotable.core.intercepter.CreateTableIntercepter;
import com.tangzc.autotable.core.intercepter.ModifyTableIntercepter;
import com.tangzc.autotable.core.strategy.CompareTableInfo;
import com.tangzc.autotable.core.strategy.IStrategy;
import com.tangzc.autotable.core.strategy.TableMetadata;
import com.tangzc.autotable.springboot.properties.AutoTableProperties;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

@Configuration
@AutoConfigureAfter({DataSourceAutoConfiguration.class, MybatisAutoConfiguration.class})
public class AutoTableAutoConfig {

    public AutoTableAutoConfig(
            SqlSessionTemplate sqlSessionTemplate,
            AutoTableProperties autoTableProperties,
            ObjectProvider<IStrategy<? extends TableMetadata, ? extends CompareTableInfo, ?>> strategies,
            ObjectProvider<AutoTableAnnotationFinder> autoTableAnnotationFinder,
            ObjectProvider<AutoTableOrmFrameAdapter> autoTableOrmFrameAdapter,
            ObjectProvider<IDataSourceHandler<?>> dynamicDataSourceHandler,
            /* 拦截器 */
            ObjectProvider<AutoTableAnnotationIntercepter> autoTableAnnotationIntercepter,
            ObjectProvider<BuildTableMetadataIntercepter> buildTableMetadataIntercepter,
            ObjectProvider<CollectEntitiesIntercepter> collectEntitiesIntercepter,
            ObjectProvider<CreateTableIntercepter> createTableIntercepter,
            ObjectProvider<ModifyTableIntercepter> modifyTableIntercepter,
            /* 回调事件 */
            ObjectProvider<CreateTableFinishCallback> createTableFinishCallbacks,
            ObjectProvider<ModifyTableFinishCallback> modifyTableFinishCallbacks,
            ObjectProvider<RunFinishCallback> runFinishCallbacks,
            ObjectProvider<ValidateFinishCallback> validateFinishCallbacks,

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

        /* 拦截器 */
        // 假如有自定义的注解拦截器，就使用自定义的注解拦截器
        autoTableAnnotationIntercepter.ifAvailable(AutoTableGlobalConfig::setAutoTableAnnotationIntercepter);
        // 假如有自定义的创建表拦截器，就使用自定义的创建表拦截器
        buildTableMetadataIntercepter.ifAvailable(AutoTableGlobalConfig::setBuildTableMetadataIntercepter);
        // 假如有自定义的收集实体拦截器，就使用自定义的收集实体拦截器
        collectEntitiesIntercepter.ifAvailable(AutoTableGlobalConfig::setCollectEntitiesIntercepter);
        // 假如有自定义的创建表拦截器，就使用自定义的创建表拦截器
        createTableIntercepter.ifAvailable(AutoTableGlobalConfig::setCreateTableIntercepter);
        // 假如有自定义的修改表拦截器，就使用自定义的修改表拦截器
        modifyTableIntercepter.ifAvailable(AutoTableGlobalConfig::setModifyTableIntercepter);

        /* 回调事件 */
        // 假如有自定义的创建表回调，就使用自定义的创建表回调
        createTableFinishCallbacks.ifAvailable(AutoTableGlobalConfig::setCreateTableFinishCallback);
        // 假如有自定义的修改表回调，就使用自定义的修改表回调
        modifyTableFinishCallbacks.ifAvailable(AutoTableGlobalConfig::setModifyTableFinishCallback);
        // 假如有自定义的运行结束回调，就使用自定义的运行结束回调
        runFinishCallbacks.ifAvailable(AutoTableGlobalConfig::setRunFinishCallback);
        // 假如有自定义的验证表回调，就使用自定义的验证表回调
        validateFinishCallbacks.ifAvailable(AutoTableGlobalConfig::setValidateFinishCallback);

        // 假如有自定义的java到数据库的转换器，就使用自定义的java到数据库的转换器
        javaTypeToDatabaseTypeConverter.ifAvailable(AutoTableGlobalConfig::setJavaTypeToDatabaseTypeConverter);
    }

    @EventListener(ContextRefreshedEvent.class)
    public void run() {

        // 启动AutoTable
        AutoTableBootstrap.start();
    }
}
