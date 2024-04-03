package com.tangzc.autotable.core.strategy.pgsql.builder;

import com.tangzc.autotable.core.builder.IndexMetadataBuilder;
import com.tangzc.autotable.core.utils.TableBeanUtils;

import java.lang.reflect.Field;

/**
 * @author don
 */
public class PgsqlIndexMetadataBuilder extends IndexMetadataBuilder {

    @Override
    protected String getDefaultIndexName(Class<?> clazz, Field field) {
        // pgsql特殊处理部分，因为Pgsql的索引名称全局需要唯一
        return TableBeanUtils.getTableName(clazz) + "_" + TableBeanUtils.getRealColumnName(clazz, field);
    }
}
