package org.dromara.autotable.springboot;

import org.dromara.autotable.core.AutoTableBootstrap;
import org.dromara.autotable.core.AutoTableGlobalConfig;
import org.dromara.autotable.core.config.PropertyConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

public class AutoTableTestExecutionListener implements TestExecutionListener {

    @Override
    public void beforeTestClass(TestContext testContext) {

        initBasePackages(testContext);

        // 单元测试模式下，启动
        AutoTableBootstrap.start();
    }

    private void initBasePackages(TestContext testContext) {
        // 初始化应用上下文，会阻塞等待上下文加载完
        ApplicationContext applicationContext = testContext.getApplicationContext();

        PropertyConfig autoTableProperties = AutoTableGlobalConfig.getAutoTableProperties();
        String[] modelPackage = autoTableProperties.getModelPackage();
        if (modelPackage == null || modelPackage.length == 0) {
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
            autoTableProperties.setModelPackage(new String[]{packageName});
        }
    }
}
