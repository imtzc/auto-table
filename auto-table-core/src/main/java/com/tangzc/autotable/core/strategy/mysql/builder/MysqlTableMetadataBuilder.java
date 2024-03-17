package com.tangzc.autotable.core.strategy.mysql.builder;

import com.tangzc.autotable.annotation.TableComment;
import com.tangzc.autotable.annotation.mysql.MysqlCharset;
import com.tangzc.autotable.annotation.mysql.MysqlEngine;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.builder.IndexMetadataBuilder;
import com.tangzc.autotable.core.strategy.mysql.data.MysqlTableMetadata;
import com.tangzc.autotable.core.utils.BeanClassUtil;
import com.tangzc.autotable.core.utils.StringUtils;
import com.tangzc.autotable.core.utils.TableBeanUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author don
 */
@Slf4j
public class MysqlTableMetadataBuilder {

    public static MysqlTableMetadata build(Class<?> clazz) {

        String tableName = TableBeanUtils.getTableName(clazz);

        MysqlTableMetadata mysqlTableMetadata = new MysqlTableMetadata(tableName);

        // 设置表注释
        TableComment tableComment = TableBeanUtils.getTableComment(clazz);
        if (tableComment != null) {
            // 获取表注释
            mysqlTableMetadata.setComment(tableComment.value());
        }

        // 设置表字符集
        String charset;
        String collate;
        MysqlCharset mysqlCharsetAnno = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(clazz, MysqlCharset.class);
        if (mysqlCharsetAnno != null) {
            charset = mysqlCharsetAnno.charset();
            collate = mysqlCharsetAnno.collate();
        } else {
            AutoTableGlobalConfig.PropertyConfig autoTableProperties = AutoTableGlobalConfig.getAutoTableProperties();
            charset = autoTableProperties.getMysql().getTableDefaultCharset();
            collate = autoTableProperties.getMysql().getTableDefaultCollation();
        }
        if (StringUtils.hasText(charset) && StringUtils.hasText(collate)) {
            // 获取表字符集
            mysqlTableMetadata.setCharacterSet(charset);
            // 字符排序
            mysqlTableMetadata.setCollate(collate);
        }

        // 获取表引擎
        MysqlEngine mysqlEngine = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(clazz, MysqlEngine.class);
        if (mysqlEngine != null) {
            mysqlTableMetadata.setEngine(mysqlEngine.value());
        }

        List<Field> fields = BeanClassUtil.listAllFieldForColumn(clazz);
        mysqlTableMetadata.setColumnMetadataList(MysqlColumnMetadataBuilder.buildList(clazz, fields));
        mysqlTableMetadata.setIndexMetadataList(IndexMetadataBuilder.of().buildList(clazz, fields));

        return mysqlTableMetadata;
    }
}
