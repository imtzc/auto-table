package com.tangzc.autotable.core.strategy.mysql.builder;

import com.tangzc.autotable.annotation.ColumnType;
import com.tangzc.autotable.annotation.mysql.MysqlColumnCharset;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.AutoTableOrmFrameAdapter;
import com.tangzc.autotable.core.builder.ColumnMetadataBuilder;
import com.tangzc.autotable.core.constants.DatabaseDialect;
import com.tangzc.autotable.core.converter.DatabaseTypeAndLength;
import com.tangzc.autotable.core.strategy.mysql.ParamValidChecker;
import com.tangzc.autotable.core.strategy.mysql.data.MysqlColumnMetadata;
import com.tangzc.autotable.core.strategy.mysql.data.MysqlTypeHelper;
import com.tangzc.autotable.core.utils.StringUtils;
import com.tangzc.autotable.core.utils.TableBeanUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * 用于存放创建表的字段信息
 *
 * @author don
 */
@Slf4j
public class MysqlColumnMetadataBuilder {

    public static List<MysqlColumnMetadata> buildList(Class<?> clazz, List<Field> fields) {

        AtomicInteger index = new AtomicInteger(1);
        return fields.stream()
                .filter(field -> TableBeanUtils.isIncludeField(field, clazz))
                .map(field -> build(clazz, field, index.getAndIncrement()))
                .collect(Collectors.toList());
    }

    public static MysqlColumnMetadata build(Class<?> clazz, Field field, int position) {

        MysqlColumnMetadata mysqlColumnMetadata = (MysqlColumnMetadata) ColumnMetadataBuilder
                .of(DatabaseDialect.MySQL, MysqlColumnMetadata::new)
                .build(clazz, field);

        // 修正默认值
        fixDefaultValue(mysqlColumnMetadata);

        // 修正类型和长度
        fixTypeAndLength(clazz, field, mysqlColumnMetadata);

        // 列顺序位置
        mysqlColumnMetadata.setPosition(position);

        // 提取并设置字符集和排序规则
        DatabaseTypeAndLength type = mysqlColumnMetadata.getType();
        extractCharsetAndCollate(field, type, (charset, collate) -> {
            // 字符集
            mysqlColumnMetadata.setCharacterSet(charset);
            // 字符排序
            mysqlColumnMetadata.setCollate(collate);
        });

        /* 基础的校验逻辑 */
        ParamValidChecker.checkColumnParam(clazz, field, mysqlColumnMetadata);

        return mysqlColumnMetadata;
    }

    private static void extractCharsetAndCollate(Field field, DatabaseTypeAndLength type, BiConsumer<String, String> charsetConsumer) {

        String charset = null;
        String collate = null;
        MysqlColumnCharset mysqlColumnCharsetAnno = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(field, MysqlColumnCharset.class);
        if (mysqlColumnCharsetAnno != null) {
            charset = mysqlColumnCharsetAnno.value();
            if (StringUtils.hasText(mysqlColumnCharsetAnno.collate())) {
                collate = mysqlColumnCharsetAnno.collate();
            }
        } else {
            // 字符类型的添加默认的字符集和排序规则
            if (MysqlTypeHelper.isCharString(type)) {
                AutoTableGlobalConfig.PropertyConfig autoTableProperties = AutoTableGlobalConfig.getAutoTableProperties();
                charset = autoTableProperties.getMysql().getColumnDefaultCharset();
                collate = autoTableProperties.getMysql().getColumnDefaultCollation();
            }
        }
        charsetConsumer.accept(charset, collate);
    }

    /**
     * 修正默认值
     */
    private static void fixDefaultValue(MysqlColumnMetadata mysqlColumnMetadata) {
        String defaultValue = mysqlColumnMetadata.getDefaultValue();
        if (StringUtils.hasText(defaultValue)) {
            DatabaseTypeAndLength type = mysqlColumnMetadata.getType();
            // 补偿逻辑：类型为Boolean的时候(实际数据库为bit数字类型)，兼容 true、false
            if (MysqlTypeHelper.isBoolean(type) && !"1".equals(defaultValue) && !"0".equals(defaultValue)) {
                if (Boolean.parseBoolean(defaultValue)) {
                    defaultValue = "1";
                } else {
                    defaultValue = "0";
                }
            }
            // 补偿逻辑：需要兼容字符串的类型，前后自动添加'
            if (MysqlTypeHelper.isCharString(type) && !defaultValue.isEmpty() && !defaultValue.startsWith("'") && !defaultValue.endsWith("'")) {
                defaultValue = "'" + defaultValue + "'";
            }
            // 补偿逻辑：时间类型，非函数的值，前后自动添加'
            if (MysqlTypeHelper.isDateTime(type) && defaultValue.matches("(\\d+.?)+") && !defaultValue.startsWith("'") && !defaultValue.endsWith("'")) {
                defaultValue = "'" + defaultValue + "'";
            }
            mysqlColumnMetadata.setDefaultValue(defaultValue);
        }
    }

    private static void fixTypeAndLength(Class<?> clazz, Field field, MysqlColumnMetadata columnMetadata) {

        DatabaseTypeAndLength type = columnMetadata.getType();
        // 如果是枚举类型，但是没有指定枚举的可选值
        if (MysqlTypeHelper.isEnum(type) && type.getValues().isEmpty()) {
            // 判断字段是不是java的枚举类型，是的话，提取所有的枚举值
            if (field.getType().isEnum()) {
                AutoTableOrmFrameAdapter autoTableOrmFrameAdapter = AutoTableGlobalConfig.getAutoTableOrmFrameAdapter();
                List<String> values = autoTableOrmFrameAdapter.getEnumValues(field.getType());
                type.setValues(values);
            } else {
                // 直接报错
                throw new RuntimeException(ColumnType.class.getSimpleName() + "value为：" + String.join(", ", MysqlTypeHelper.ENUM_OR_SET_TYPE) + "的时候，"
                        + clazz.getSimpleName() + "." + field.getName() + "必须是枚举类型或者指定" + ColumnType.class.getSimpleName() + "的values");
            }
        }
    }
}
