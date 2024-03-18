package com.tangzc.autotable.core.builder;

import com.tangzc.autotable.annotation.ColumnDefault;
import com.tangzc.autotable.annotation.enums.DefaultValueEnum;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.strategy.ColumnMetadata;
import com.tangzc.autotable.core.utils.TableBeanUtils;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 用于存放创建表的字段信息
 *
 * @author don
 */
@Slf4j
@AllArgsConstructor(staticName = "of")
public class ColumnMetadataBuilder {

    @NonNull
    private final String databaseDialect;
    private final Supplier<ColumnMetadata> columnMetadataSupplier;

    public static ColumnMetadataBuilder of(String databaseDialect) {
        return of(databaseDialect, ColumnMetadata::new);
    }

    public List<ColumnMetadata> buildList(Class<?> clazz, List<Field> fields) {

        List<ColumnMetadata> columnMetadata = fields.stream()
                .filter(field -> TableBeanUtils.isIncludeField(field, clazz))
                .map(field -> this.build(clazz, field)
                )
                .collect(Collectors.toList());

        if (columnMetadata.isEmpty()) {
            log.warn("扫描发现{}没有建表字段请注意！", clazz.getName());
        }

        return columnMetadata;
    }

    public ColumnMetadata build(Class<?> clazz, Field field) {

        ColumnMetadata columnMetadata = columnMetadataSupplier.get();
        columnMetadata.setName(TableBeanUtils.getRealColumnName(clazz, field))
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

        return columnMetadata;
    }
}
