package com.tangzc.autotable.core.converter;

import com.tangzc.autotable.annotation.ColumnType;
import com.tangzc.autotable.core.AutoTableGlobalConfig;
import com.tangzc.autotable.core.constants.DatabaseDialect;
import com.tangzc.autotable.core.utils.StringUtils;
import com.tangzc.autotable.core.utils.TableBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
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
     * @param typeEnumMap     字段类型-》数据库类型 映射
     */
    static void addTypeMapping(String databaseDialect, Map<Class<?>, DefaultTypeEnumInterface> typeEnumMap) {
        JAVA_TO_DB_TYPE_MAPPING.computeIfAbsent(databaseDialect, k -> new HashMap<>()).putAll(typeEnumMap);
    }

    /**
     * java转数据库类型
     *
     * @param databaseDialect 数据库类型，参考{@link DatabaseDialect}中的常量
     * @param clazz           实体类
     * @param field           字段
     * @return 数据库类型
     */
    default DatabaseTypeAndLength convert(String databaseDialect, Class<?> clazz, Field field) {

        ColumnType column = TableBeanUtils.getColumnType(field);
        // 设置了类型
        if (column != null) {
            String type = column.value();
            Integer length = column.length() > -1 ? column.length() : null;
            Integer decimalLength = column.decimalLength() > -1 ? column.decimalLength() : null;
            List<String> values = Arrays.asList(column.values());
            // 如果明确指定了类型名，直接替换
            if (StringUtils.hasText(type)) {
                return new DatabaseTypeAndLength(type, length, decimalLength, values);
            }
            // 如果没有指定明确的类型名，但是却指定了长度。那么使用默认类型+指定长度
            if (length != null || decimalLength != null) {
                DatabaseTypeAndLength typeAndLength = getDatabaseTypeAndLength(databaseDialect, clazz, field);
                typeAndLength.setLength(length);
                typeAndLength.setDecimalLength(decimalLength);
                return typeAndLength;
            }
        }
        // 其他情况，使用默认类型
        return getDatabaseTypeAndLength(databaseDialect, clazz, field);
    }

    /**
     * 获取数据库类型
     *
     * @param databaseDialect 数据库类型，参考{@link DatabaseDialect}中的常量
     * @param clazz           实体类
     * @param field           字段
     * @return 数据库类型
     */
    default DatabaseTypeAndLength getDatabaseTypeAndLength(String databaseDialect, Class<?> clazz, Field field) {

        DatabaseTypeAndLength typeAndLength;

        Map<Class<?>, DefaultTypeEnumInterface> typeMap = JAVA_TO_DB_TYPE_MAPPING.getOrDefault(databaseDialect, Collections.emptyMap());
        if (typeMap.isEmpty()) {
            log.warn("数据库方言{}没有找到对应的数据库类型映射关系", databaseDialect);
        }

        Class<?> fieldClass;
        // 处理泛型
        if (field.getGenericType() instanceof TypeVariable) {
            // 无法通过直接获取类型，真是字段类型需要从实现类的接口泛型中获取
            fieldClass = getFieldGenericType(clazz, field);
        } else {
            fieldClass = TableBeanUtils.getFieldType(clazz, field);
        }

        DefaultTypeEnumInterface sqlType = typeMap.get(fieldClass);
        if (sqlType == null) {
            log.warn("{}下的字段{}在{}下找不到对应的数据库类型，默认使用了字符串类型，如果想自定义，请调用JavaTypeToDatabaseTypeConverter.addTypeMap(DatabaseDialect.{}, {}.class, ?)添加映射关系", clazz.getName(), fieldClass.getSimpleName(), databaseDialect, databaseDialect, fieldClass.getSimpleName());
            sqlType = typeMap.get(String.class);
        }
        typeAndLength = new DatabaseTypeAndLength(sqlType.getTypeName(), sqlType.getDefaultLength(), sqlType.getDefaultDecimalLength(), Collections.emptyList());
        return typeAndLength;
    }

    /**
     * 获取指定类中某字段的泛型类型
     *
     * @param clazz 子类的Class对象
     * @param field 要获取的字段
     * @return 字段的泛型类型
     */
    default Class<?> getFieldGenericType(Class<?> clazz, Field field) {
        // 获取父类的泛型类型信息
        Type genericSuperclass = clazz.getGenericSuperclass();

        if (!(genericSuperclass instanceof ParameterizedType)) {
            return null; // 如果没有泛型信息，则返回null
        }

        ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;

        // 获取BaseEntity的泛型参数列表
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

        // 获取字段的泛型类型
        Type genericFieldType = field.getGenericType();

        // 在父类的泛型参数中找到对应的实际类型
        String typeVariableName = ((TypeVariable<?>) genericFieldType).getName();
        // 遍历父类的泛型参数，找到对应的具体类型
        Class<?> declaringClass = field.getDeclaringClass();
        TypeVariable<? extends Class<?>>[] typeParameters = declaringClass.getTypeParameters();
        for (int i = 0; i < typeParameters.length; i++) {
            if (typeParameters[i].getName().equals(typeVariableName)) {
                genericFieldType = actualTypeArguments[i];
                break;
            }
        }

        // 如果字段不是泛型变量，直接返回它的类型
        if (genericFieldType instanceof Class<?>) {
            return ((Class<?>) genericFieldType);
        }

        return field.getType();
    }
}
