package com.tangzc.autotable.core.strategy.pgsql.builder;

import com.tangzc.autotable.core.builder.DefaultTableMetadataBuilder;
import com.tangzc.autotable.core.builder.IndexMetadataBuilder;
import com.tangzc.autotable.core.dynamicds.SqlSessionFactoryManager;
import com.tangzc.autotable.core.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.Configuration;

import java.sql.Connection;

/**
 * @author don
 */
@Slf4j
public class PgsqlTableMetadataBuilder extends DefaultTableMetadataBuilder {

    public PgsqlTableMetadataBuilder() {
        super(new PgsqlColumnMetadataBuilder(), new IndexMetadataBuilder());
    }

    @Override
    protected String getTableSchema(Class<?> clazz) {
        String tableSchema = super.getTableSchema(clazz);
        if (StringUtils.noText(tableSchema)) {
            // 获取Configuration对象
            Configuration configuration = SqlSessionFactoryManager.getSqlSessionFactory().getConfiguration();
            try (Connection connection = configuration.getEnvironment().getDataSource().getConnection()) {
                // 通过连接获取DatabaseMetaData对象
                return connection.getSchema();
            } catch (Exception e) {
                log.error("获取数据库信息失败", e);
            }
            return "public";
        }
        return tableSchema;
    }
}
