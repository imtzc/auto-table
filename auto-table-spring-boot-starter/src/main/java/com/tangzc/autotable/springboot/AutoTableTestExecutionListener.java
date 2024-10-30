package com.tangzc.autotable.springboot;

import com.tangzc.autotable.core.AutoTableBootstrap;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.config.PropertyConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

public class AutoTableTestExecutionListener implements TestExecutionListener {

    @Override
    public void beforeTestClass(TestContext testContext) {
        // 获取应用上下文
        ApplicationContext applicationContext = testContext.getApplicationContext();

        // 获取启动类（带有@SpringBootApplication注解的类）
        Object mainClass = applicationContext.getBeansWithAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class)
                .values()
                .stream()
                .findFirst()
                .orElseGet(() -> applicationContext.getBeansWithAnnotation(org.springframework.boot.SpringBootConfiguration.class)
                        .values()
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("启动类未找到"))
                );

        // 获取启动类的包名
        String packageName = mainClass.getClass().getPackage().getName();

        // 当默认启动包名为空时，配置默认启动包名
        PropertyConfig autoTableProperties = AutoTableGlobalConfig.getAutoTableProperties();
        if(autoTableProperties.getModelPackage() == null || autoTableProperties.getModelPackage().length == 0) {
            autoTableProperties.setModelPackage(new String[]{packageName});
        }

        // 单元测试模式下，启动
        AutoTableBootstrap.start();
    }
}
