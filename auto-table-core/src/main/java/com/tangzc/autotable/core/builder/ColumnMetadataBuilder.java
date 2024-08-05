package com.tangzc.autotable.core.builder;

import com.tangzc.autotable.annotation.ColumnDefault;
import com.tangzc.autotable.annotation.enums.DefaultValueEnum;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.converter.DatabaseTypeAndLength;
import com.tangzc.autotable.core.strategy.ColumnMetadata;
import com.tangzc.autotable.core.utils.TableBeanUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 用于存放创建表的字段信息
 *
 * @author don
 */
@Slf4j
public class ColumnMetadataBuilder {

    protected final String databaseDialect;

    public ColumnMetadataBuilder(String databaseDialect) {
        this.databaseDialect = databaseDialect;
    }

    public <T extends ColumnMetadata> List<T> buildList(Class<?> clazz, List<Field> fields) {

        AtomicInteger index = new AtomicInteger(1);
        List<ColumnMetadata> columnMetadata = fields.stream()
                .filter(field -> TableBeanUtils.isIncludeField(field, clazz))
                .map(field -> this.build(clazz, field, index.getAndIncrement()))
                .collect(Collectors.toList());

        if (columnMetadata.isEmpty()) {
            log.warn("扫描发现{}没有建表字段请注意！", clazz.getName());
        }

        return (List<T>) columnMetadata;
    }

    public ColumnMetadata build(Class<?> clazz, Field field, int position) {

        ColumnMetadata columnMetadata = newColumnMetadata();
        DatabaseTypeAndLength typeAndLength = getTypeAndLength(databaseDialect, clazz, field);
        columnMetadata.setName(TableBeanUtils.getRealColumnName(clazz, field))
                .setComment(TableBeanUtils.getComment(field, clazz))
                .setType(typeAndLength)
                .setNotNull(TableBeanUtils.isNotNull(field, clazz))
                .setPrimary(TableBeanUtils.isPrimary(field, clazz))
                .setAutoIncrement(TableBeanUtils.isAutoIncrement(field, clazz));
        ColumnDefault columnDefault = TableBeanUtils.getDefaultValue(field);
        if (columnDefault != null) {

            DefaultValueEnum defaultValueType = columnDefault.type();
            columnMetadata.setDefaultValueType(defaultValueType);

            String defaultValue = getDefaultValue(typeAndLength, columnDefault);
            columnMetadata.setDefaultValue(defaultValue);
        }

        // 预留填充逻辑
        customBuild(columnMetadata, clazz, field, position);

        return columnMetadata;
    }

    protected void customBuild(ColumnMetadata columnMetadata, Class<?> clazz, Field field, int position) {

    }

    protected DatabaseTypeAndLength getTypeAndLength(String databaseDialect, Class<?> clazz, Field field) {
        return AutoTableGlobalConfig.getJavaTypeToDatabaseTypeConverter().convert(databaseDialect, clazz, field);
    }

    protected String getDefaultValue(DatabaseTypeAndLength typeAndLength, ColumnDefault columnDefault) {
        String defaultValue = columnDefault.value();
        // 因为空字符串，必须由DefaultValueEnum.EMPTY_STRING来表示，所以这里要特殊处理
        if (defaultValue != null && defaultValue.isEmpty()) {
            defaultValue = null;
        }
        return defaultValue;
    }

    protected ColumnMetadata newColumnMetadata() {
        return new ColumnMetadata();
    }
}
