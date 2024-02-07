package com.tangzc.autotable.core.strategy.mysql.builder;

import com.tangzc.autotable.annotation.TableComment;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.autotable.annotation.mysql.MysqlCharset;
import com.tangzc.autotable.annotation.mysql.MysqlEngine;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.strategy.mysql.data.MysqlColumnMetadata;
import com.tangzc.autotable.core.strategy.mysql.data.MysqlIndexMetadata;
import com.tangzc.autotable.core.strategy.mysql.data.MysqlTableMetadata;
import com.tangzc.autotable.core.utils.BeanClassUtil;
import com.tangzc.autotable.core.utils.IndexRepeatChecker;
import com.tangzc.autotable.core.utils.TableBeanUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author don
 */
@Slf4j
public class MysqlTableMetadataBuilder {

    public static MysqlTableMetadata build(Class<?> clazz) {

        String tableName = TableBeanUtils.getTableName(clazz);

        MysqlTableMetadata mysqlTableMetadata = new MysqlTableMetadata();
        mysqlTableMetadata.setTableName(tableName);

        TableComment tableComment = TableBeanUtils.getTableComment(clazz);
        if(tableComment != null) {
            // 获取表注释
            mysqlTableMetadata.setComment(tableComment.value());
        }

        AutoTableGlobalConfig.PropertyConfig autoTableProperties = AutoTableGlobalConfig.getAutoTableProperties();
        String charset = autoTableProperties.getMysql().getTableDefaultCharset();
        String collate = autoTableProperties.getMysql().getTableDefaultCollation();
        MysqlCharset mysqlCharsetAnno = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(clazz, MysqlCharset.class);
        if(mysqlCharsetAnno != null) {
            charset = mysqlCharsetAnno.charset();
            collate = mysqlCharsetAnno.collate();
        }
        // 获取表字符集
        mysqlTableMetadata.setCharacterSet(charset);
        // 字符排序
        mysqlTableMetadata.setCollate(collate);

        // 获取表引擎
        MysqlEngine mysqlEngine = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(clazz, MysqlEngine.class);
        if (mysqlEngine != null) {
            mysqlTableMetadata.setEngine(mysqlEngine.value());
        }

        List<Field> fields = BeanClassUtil.getAllDeclaredFieldsExcludeStatic(clazz);
        mysqlTableMetadata.setColumnMetadataList(getColumnList(clazz, fields));
        mysqlTableMetadata.setIndexMetadataList(getIndexList(clazz, fields));

        return mysqlTableMetadata;
    }

    public static List<MysqlColumnMetadata> getColumnList(Class<?> clazz, List<Field> fields) {
        AtomicInteger index = new AtomicInteger(1);
        return fields.stream()
                .filter(field -> TableBeanUtils.isIncludeField(field, clazz))
                .map(field -> MysqlColumnMetadataBuilder.build(clazz, field, index.getAndIncrement()))
                .collect(Collectors.toList());
    }

    public static List<MysqlIndexMetadata> getIndexList(Class<?> clazz, List<Field> fields) {

        IndexRepeatChecker indexRepeatChecker = IndexRepeatChecker.of();

        // 类上的索引注解
        List<TableIndex> tableIndexes = TableBeanUtils.getTableIndexes(clazz);
        List<MysqlIndexMetadata> indexMetadataList = tableIndexes.stream()
                .map(tableIndex -> MysqlIndexMetadataBuilder.build(clazz, tableIndex, AutoTableGlobalConfig.getAutoTableProperties().getIndexPrefix()))
                .filter(Objects::nonNull)
                .filter(indexMetadata -> indexRepeatChecker.filter(indexMetadata.getName()))
                .collect(Collectors.toList());

        // 字段上的索引注解
        List<MysqlIndexMetadata> onFieldIndexMetadata = fields.stream()
                .filter(field -> TableBeanUtils.isIncludeField(field, clazz))
                .map(field -> MysqlIndexMetadataBuilder.build(clazz, field, AutoTableGlobalConfig.getAutoTableProperties().getIndexPrefix()))
                .filter(Objects::nonNull)
                .filter(indexMetadata -> indexRepeatChecker.filter(indexMetadata.getName()))
                .collect(Collectors.toList());
        indexMetadataList.addAll(onFieldIndexMetadata);
        return indexMetadataList;
    }
}
