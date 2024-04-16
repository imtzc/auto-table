package com.tangzc.autotable.core.strategy.mysql.builder;

import com.tangzc.autotable.annotation.TableComment;
import com.tangzc.autotable.annotation.mysql.MysqlCharset;
import com.tangzc.autotable.annotation.mysql.MysqlEngine;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.builder.IndexMetadataBuilder;
import com.tangzc.autotable.core.config.PropertyConfig;
import com.tangzc.autotable.core.strategy.IndexMetadata;
import com.tangzc.autotable.core.strategy.mysql.data.MysqlColumnMetadata;
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
        TableComment tableCommentAnno = TableBeanUtils.getTableComment(clazz);
        String tableComment = tableCommentAnno == null ? null : tableCommentAnno.value();
        MysqlTableMetadata mysqlTableMetadata = new MysqlTableMetadata(clazz, tableName, tableComment);

        // 设置表字符集
        String charset;
        String collate;
        MysqlCharset mysqlCharsetAnno = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(clazz, MysqlCharset.class);
        if (mysqlCharsetAnno != null) {
            charset = mysqlCharsetAnno.charset();
            collate = mysqlCharsetAnno.collate();
        } else {
            PropertyConfig autoTableProperties = AutoTableGlobalConfig.getAutoTableProperties();
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

        List<MysqlColumnMetadata> columnMetadataList = new MysqlColumnMetadataBuilder().buildList(clazz, fields);
        mysqlTableMetadata.setColumnMetadataList(columnMetadataList);
        List<IndexMetadata> indexMetadataList = new IndexMetadataBuilder().buildList(clazz, fields);
        mysqlTableMetadata.setIndexMetadataList(indexMetadataList);

        return mysqlTableMetadata;
    }
}
