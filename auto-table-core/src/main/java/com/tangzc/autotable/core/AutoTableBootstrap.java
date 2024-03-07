package com.tangzc.autotable.core;

import com.tangzc.autotable.annotation.Ignore;
import com.tangzc.autotable.annotation.TableComment;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.autotable.annotation.TableIndexes;
import com.tangzc.autotable.annotation.TableName;
import com.tangzc.autotable.annotation.mysql.MysqlCharset;
import com.tangzc.autotable.annotation.mysql.MysqlEngine;
import com.tangzc.autotable.core.dynamicds.IDataSourceHandler;
import com.tangzc.autotable.core.strategy.IStrategy;
import com.tangzc.autotable.core.strategy.mysql.MysqlStrategy;
import com.tangzc.autotable.core.strategy.pgsql.PgsqlStrategy;
import com.tangzc.autotable.core.strategy.sqlite.SqliteStrategy;
import com.tangzc.autotable.core.utils.ClassScanner;
import com.tangzc.autotable.core.utils.TableBeanUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 启动时进行处理的实现类
 *
 * @author chenbin.sun
 */
@Slf4j
public class AutoTableBootstrap {

    public static void start() {

        AutoTableGlobalConfig.PropertyConfig autoTableProperties = AutoTableGlobalConfig.getAutoTableProperties();

        // 判断模式，非none，才执行逻辑
        if (autoTableProperties.getMode() == RunMode.none) {
            return;
        }

        if (autoTableProperties.getShowBanner()) {
            Banner.print();
        }

        final long start = System.currentTimeMillis();

        // 注册内置的不同数据源策略
        AutoTableGlobalConfig.addStrategy(new MysqlStrategy());
        AutoTableGlobalConfig.addStrategy(new PgsqlStrategy());
        AutoTableGlobalConfig.addStrategy(new SqliteStrategy());

        // 获取扫描包路径
        String[] packs = getModelPackage(autoTableProperties);

        // 从包package中获取所有的Class
        Set<Class<? extends Annotation>> includeAnnotations = new HashSet<>(
                Arrays.asList(TableName.class, TableComment.class, TableIndexes.class, TableIndex.class,
                        MysqlEngine.class, MysqlCharset.class)
        );
        // 添加自定义的注解
        includeAnnotations.addAll(AutoTableGlobalConfig.getAutoTableOrmFrameAdapter().scannerAnnotations());
        Set<Class<? extends Annotation>> ignoreAnnotations = new HashSet<>(Collections.singleton(Ignore.class));
        // 经过自定义的拦截器，修改最终影响自动建表的注解
        AutoTableGlobalConfig.getAutoTableAnnotationIntercepter().intercept(includeAnnotations, ignoreAnnotations);
        // 扫描所有的类，过滤出指定注解的实体
        Set<Class<?>> classes = ClassScanner.scan(packs, includeAnnotations, ignoreAnnotations);

        // 检查重名的表
        Map<String, Long> repeatCheckMap = classes.stream().map(TableBeanUtils::getTableName).collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        for (Map.Entry<String, Long> repeatCheckItem : repeatCheckMap.entrySet()) {
            Long sameTableNameCount = repeatCheckItem.getValue();
            if (sameTableNameCount > 1) {
                String tableName = repeatCheckItem.getKey();
                throw new RuntimeException("存在重名的表：" + tableName + "，请检查！");
            }
        }

        // 获取对应的数据源，根据不同数据库方言，执行不同的处理
        IDataSourceHandler<?> datasourceHandler = AutoTableGlobalConfig.getDatasourceHandler();
        datasourceHandler.handleAnalysis(classes, (databaseDialect, entityClass) -> {
            log.info("数据库方言（" + databaseDialect + "）");
            // 查找对应的数据源策略
            IStrategy<?, ?, ?> databaseStrategy = AutoTableGlobalConfig.getStrategy(databaseDialect);
            if (databaseStrategy != null) {
                databaseStrategy.start(entityClass);
            } else {
                log.warn("没有找到对应的数据库（" + databaseDialect + "）方言策略，无法执行自动建表");
            }
        });
        log.info("AutoTable执行结束。耗时：{}", (System.currentTimeMillis() - start) + "ms");
    }

    private static String[] getModelPackage(AutoTableGlobalConfig.PropertyConfig autoTableProperties) {
        String[] packs = autoTableProperties.getModelPackage();
        if (packs == null) {
            packs = new String[]{getBootPackage()};
        }
        return packs;
    }

    private static String getBootPackage() {
        StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            if ("main".equals(stackTraceElement.getMethodName())) {
                String mainClassName = stackTraceElement.getClassName();
                int lastDotIndex = mainClassName.lastIndexOf(".");
                return (lastDotIndex != -1 ? mainClassName.substring(0, lastDotIndex) : "");
            }
        }
        throw new RuntimeException("未找到主默认包");
    }
}
