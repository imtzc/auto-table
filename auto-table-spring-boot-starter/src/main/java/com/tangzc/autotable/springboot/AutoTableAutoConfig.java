package com.tangzc.autotable.springboot;

import com.tangzc.autotable.core.AutoTableAnnotationFinder;
import com.tangzc.autotable.core.AutoTableBootstrap;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.AutoTableOrmFrameAdapter;
import com.tangzc.autotable.core.converter.JavaTypeToDatabaseTypeConverter;
import com.tangzc.autotable.core.dynamicds.IDataSourceHandler;
import com.tangzc.autotable.core.dynamicds.SqlSessionFactoryManager;
import com.tangzc.autotable.core.intercepter.BuildTableMetadataIntercepter;
import com.tangzc.autotable.core.strategy.CompareTableInfo;
import com.tangzc.autotable.core.strategy.IStrategy;
import com.tangzc.autotable.core.strategy.TableMetadata;
import com.tangzc.autotable.springboot.properties.AutoTableProperties;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@AutoConfigureAfter({DataSourceAutoConfiguration.class, MybatisAutoConfiguration.class})
public class AutoTableAutoConfig implements ApplicationRunner {

    private final SqlSessionTemplate sqlSessionTemplate;
    private final AutoTableProperties autoTableProperties;

    @Autowired(required = false)
    private List<IStrategy<? extends TableMetadata, ? extends CompareTableInfo, ?>> strategies;

    @Autowired(required = false)
    private AutoTableAnnotationFinder autoTableAnnotationFinder;

    @Autowired(required = false)
    private AutoTableOrmFrameAdapter autoTableOrmFrameAdapter;

    @Autowired(required = false)
    private BuildTableMetadataIntercepter buildTableMetadataIntercepter;

    @Autowired(required = false)
    private IDataSourceHandler<?> dynamicDataSourceHandler;

    @Autowired(required = false)
    private JavaTypeToDatabaseTypeConverter javaTypeToDatabaseTypeConverter;

    public AutoTableAutoConfig(SqlSessionTemplate sqlSessionTemplate, AutoTableProperties autoTableProperties) {
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.autoTableProperties = autoTableProperties;
    }

    @Override
    public void run(ApplicationArguments args) {

        // 默认设置全局的SqlSessionFactory
        SqlSessionFactoryManager.setSqlSessionFactory(sqlSessionTemplate.getSqlSessionFactory());

        // 设置全局的配置
        AutoTableGlobalConfig.setAutoTableProperties(autoTableProperties.toConfig());

        // 假如有自定的注解扫描器，就使用自定义的注解扫描器
        if (autoTableAnnotationFinder != null) {
            AutoTableGlobalConfig.setAutoTableAnnotationFinder(autoTableAnnotationFinder);
        } else {
            // 没有，则设置内置的注解扫描器
            AutoTableGlobalConfig.setAutoTableAnnotationFinder(new CustomAnnotationFinder());
        }

        // 如果有自定义的数据库策略，则加载
        if (strategies != null && !strategies.isEmpty()) {
            for (IStrategy<? extends TableMetadata, ? extends CompareTableInfo, ?> strategy : strategies) {
                AutoTableGlobalConfig.addStrategy(strategy);
            }
        }

        // 假如有自定义的orm框架适配器，就使用自定义的orm框架适配器
        if (autoTableOrmFrameAdapter != null) {
            AutoTableGlobalConfig.setAutoTableOrmFrameAdapter(autoTableOrmFrameAdapter);
        }

        // 假如有自定义的动态数据源处理器，就使用自定义的动态数据源处理器
        if (dynamicDataSourceHandler != null) {
            AutoTableGlobalConfig.setDatasourceHandler(dynamicDataSourceHandler);
        }

        // 假如有自定义的创建表拦截器，就使用自定义的创建表拦截器
        if (buildTableMetadataIntercepter != null) {
            AutoTableGlobalConfig.setBuildTableMetadataIntercepter(buildTableMetadataIntercepter);
        }

        // 假如有自定义的java到数据库的转换器，就使用自定义的java到数据库的转换器
        if (javaTypeToDatabaseTypeConverter != null) {
            AutoTableGlobalConfig.setJavaTypeToDatabaseTypeConverter(javaTypeToDatabaseTypeConverter);
        }

        // 启动AutoTable
        AutoTableBootstrap.start();
    }
}
