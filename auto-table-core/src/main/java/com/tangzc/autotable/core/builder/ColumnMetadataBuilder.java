package com.tangzc.autotable.core.builder;

import com.tangzc.autotable.annotation.ColumnDefault;
import com.tangzc.autotable.annotation.enums.DefaultValueEnum;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.strategy.ColumnMetadata;
import com.tangzc.autotable.core.utils.TableBeanUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.function.Consumer;

/**
 * 用于存放创建表的字段信息
 *
 * @author don
 */
@Slf4j
public class ColumnMetadataBuilder {

    private ColumnMetadata columnMetadata;
    private String databaseDialect;

    private ColumnMetadataBuilder() {
    }

    public static ColumnMetadataBuilder of(String databaseDialect, ColumnMetadata columnMetadata) {
        ColumnMetadataBuilder columnMetadataBuilder = new ColumnMetadataBuilder();
        columnMetadataBuilder.columnMetadata = columnMetadata;
        columnMetadataBuilder.databaseDialect = databaseDialect;
        return columnMetadataBuilder;
    }

    public ColumnMetadata buildFromAnnotation(Class<?> clazz, Field field) {
        return buildFromAnnotation(clazz, field, null);
    }

    public ColumnMetadata buildFromAnnotation(Class<?> clazz, Field field, Consumer<ColumnMetadata> convertor) {

        columnMetadata
                .setName(TableBeanUtils.getRealColumnName(clazz, field))
                .setComment(TableBeanUtils.getComment(field))
                .setType(AutoTableGlobalConfig.getJavaTypeToDatabaseTypeConverter().convert(databaseDialect, clazz, field))
                .setNotNull(TableBeanUtils.isNotNull(field, clazz))
                .setPrimary(TableBeanUtils.isPrimary(field, clazz))
                .setAutoIncrement(TableBeanUtils.isAutoIncrement(field, clazz));
        ColumnDefault columnDefault = TableBeanUtils.getDefaultValue(field);
        if (columnDefault != null) {
            DefaultValueEnum defaultValueType = columnDefault.type();
            String defaultValue = columnDefault.value();
            // 因为空字符串，必须由DefaultValueEnum.EMPTY_STRING来表示，所以这里要特殊处理
            if (defaultValue != null && defaultValue.isEmpty()) {
                defaultValue = null;
            }
            columnMetadata.setDefaultValueType(defaultValueType)
                    .setDefaultValue(defaultValue);
        }

        if (convertor != null) {
            convertor.accept(columnMetadata);
        }

        return columnMetadata;
    }
}
