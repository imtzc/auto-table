package com.tangzc.autotable.core.strategy.mysql.builder;

import com.tangzc.autotable.annotation.ColumnDefault;
import com.tangzc.autotable.annotation.ColumnType;
import com.tangzc.autotable.annotation.mysql.MysqlColumnCharset;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.AutoTableOrmFrameAdapter;
import com.tangzc.autotable.core.constants.DatabaseDialect;
import com.tangzc.autotable.core.converter.DatabaseTypeAndLength;
import com.tangzc.autotable.core.converter.JavaTypeToDatabaseTypeConverter;
import com.tangzc.autotable.core.strategy.mysql.ParamValidChecker;
import com.tangzc.autotable.core.strategy.mysql.data.MysqlColumnMetadata;
import com.tangzc.autotable.core.strategy.mysql.data.MysqlTypeHelper;
import com.tangzc.autotable.core.utils.StringUtils;
import com.tangzc.autotable.core.utils.TableBeanUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 用于存放创建表的字段信息
 *
 * @author don
 */
@Slf4j
public class MysqlColumnMetadataBuilder {

    public static MysqlColumnMetadata build(Class<?> clazz, Field field, int position) {
        MysqlColumnMetadata mysqlColumnMetadata = new MysqlColumnMetadata();
        mysqlColumnMetadata.setName(TableBeanUtils.getRealColumnName(clazz, field));
        mysqlColumnMetadata.setType(getTypeAndLength(field, clazz));
        mysqlColumnMetadata.setNotNull(TableBeanUtils.isNotNull(field, clazz));
        mysqlColumnMetadata.setPrimary(TableBeanUtils.isPrimary(field, clazz));
        mysqlColumnMetadata.setAutoIncrement(TableBeanUtils.isAutoIncrement(field, clazz));
        mysqlColumnMetadata.setPosition(position);

        String charset = null;
        String collate = null;
        MysqlColumnCharset mysqlColumnCharsetAnno = AutoTableGlobalConfig.getAutoTableAnnotationFinder().find(field, MysqlColumnCharset.class);
        if (mysqlColumnCharsetAnno != null) {
            charset = mysqlColumnCharsetAnno.value();
            if (StringUtils.hasText(mysqlColumnCharsetAnno.collate())) {
                collate = mysqlColumnCharsetAnno.collate();
            }
        } else {
            DatabaseTypeAndLength type = mysqlColumnMetadata.getType();
            // 字符类型的添加默认的字符集和排序规则
            if (MysqlTypeHelper.isCharString(type)) {
                AutoTableGlobalConfig.PropertyConfig autoTableProperties = AutoTableGlobalConfig.getAutoTableProperties();
                charset = autoTableProperties.getMysql().getColumnDefaultCharset();
                collate = autoTableProperties.getMysql().getColumnDefaultCollation();
            }
        }
        // 字符集
        mysqlColumnMetadata.setCharacterSet(charset);
        // 字符排序
        mysqlColumnMetadata.setCollate(collate);

        ColumnDefault columnDefault = TableBeanUtils.getDefaultValue(field);
        if (columnDefault != null) {
            mysqlColumnMetadata.setDefaultValueType(columnDefault.type());
            String defaultValue = columnDefault.value();
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
        mysqlColumnMetadata.setComment(TableBeanUtils.getComment(field));

        /* 基础的校验逻辑 */
        ParamValidChecker.checkColumnParam(clazz, field, mysqlColumnMetadata);

        return mysqlColumnMetadata;
    }

    private static DatabaseTypeAndLength getTypeAndLength(Field field, Class<?> clazz) {

        // 类型为空根据字段类型去默认匹配类型
        JavaTypeToDatabaseTypeConverter typeConverter = AutoTableGlobalConfig.getJavaTypeToDatabaseTypeConverter();
        DatabaseTypeAndLength type = typeConverter.convert(DatabaseDialect.MYSQL, clazz, field);

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

        return type;
    }
}
