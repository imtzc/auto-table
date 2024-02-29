package com.tangzc.autotable.core.converter;

import com.tangzc.autotable.annotation.ColumnType;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.constants.DatabaseDialect;
import com.tangzc.autotable.core.utils.StringUtils;
import com.tangzc.autotable.core.utils.TableBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义java转数据库的类型转换器
 *
 * @author don
 */
public interface JavaTypeToDatabaseTypeConverter {

    Logger log = LoggerFactory.getLogger(JavaTypeToDatabaseTypeConverter.class);

    /**
     * 类型映射，在注册数据库策略的时候，注入进来，详见{@link AutoTableGlobalConfig#addStrategy}
     */
    Map<String, Map<Class<?>, DefaultTypeEnumInterface>> JAVA_TO_DB_TYPE_MAPPING = new HashMap<>();

    /**
     * 添加类型映射
     *
     * @param databaseDialect 数据库类型，参考{@link DatabaseDialect}中的常量
     * @param clazz           字段类型
     * @param typeEnum        数据库类型
     */
    static void addTypeMapping(String databaseDialect, Class<?> clazz, DefaultTypeEnumInterface typeEnum) {
        JAVA_TO_DB_TYPE_MAPPING.computeIfAbsent(databaseDialect, k -> new HashMap<>()).put(clazz, typeEnum);
    }

    /**
     * 添加类型映射
     *
     * @param databaseDialect 数据库类型，参考{@link DatabaseDialect}中的常量
     * @param typeEnumMap        字段类型-》数据库类型 映射
     */
    static void addTypeMapping(String databaseDialect, Map<Class<?>, DefaultTypeEnumInterface> typeEnumMap) {
        JAVA_TO_DB_TYPE_MAPPING.computeIfAbsent(databaseDialect, k -> new HashMap<>()).putAll(typeEnumMap);
    }

    /**
     * java转数据库类型
     */
    default DatabaseTypeAndLength convert(String databaseDialect, Class<?> clazz, Field field) {

        DatabaseTypeAndLength typeAndLength = getDatabaseTypeAndLength(databaseDialect, clazz, field);

        ColumnType column = TableBeanUtils.getColumnType(field);
        // 设置了类型
        if (column != null) {
            String type = column.value();
            Integer length = column.length() > -1 ? column.length() : null;
            Integer decimalLength = column.decimalLength() > -1 ? column.decimalLength() : null;
            List<String> values = Arrays.asList(column.values());
            // 如果明确指定了类型名，直接替换
            if(StringUtils.hasText(type)) {
                typeAndLength = new DatabaseTypeAndLength(type, length, decimalLength, values);
            } else {
                // 如果没有指定明确的类型名，但是却指定了长度，使用指定长度
                if (length != null || decimalLength != null) {
                    typeAndLength.setLength(length);
                    typeAndLength.setDecimalLength(decimalLength);
                }
            }
        }

        return typeAndLength;
    }

    default DatabaseTypeAndLength getDatabaseTypeAndLength(String databaseDialect, Class<?> clazz, Field field) {

        DatabaseTypeAndLength typeAndLength;
        Class<?> fieldClass = TableBeanUtils.getFieldType(clazz, field);

        Map<Class<?>, DefaultTypeEnumInterface> typeMap = JAVA_TO_DB_TYPE_MAPPING.getOrDefault(databaseDialect, Collections.emptyMap());
        if (typeMap.isEmpty()) {
            log.warn("数据库方言" + databaseDialect + "没有找到对应的数据库类型映射关系");
        }

        DefaultTypeEnumInterface sqlType = typeMap.get(fieldClass);

        if (sqlType == null) {
            log.warn("{}下的字段{}在{}下找不到对应的数据库类型，默认使用了字符串类型，如果想自定义，请调用JavaTypeToDatabaseTypeConverter.addTypeMap(DatabaseDialect.{}, {}.class, ?)添加映射关系", clazz.getName(), fieldClass.getSimpleName(), databaseDialect, databaseDialect, fieldClass.getSimpleName());
            sqlType = typeMap.get(String.class);
        }
        typeAndLength = new DatabaseTypeAndLength(sqlType.getTypeName(), sqlType.getDefaultLength(), sqlType.getDefaultDecimalLength(), Collections.emptyList());
        return typeAndLength;
    }
}
