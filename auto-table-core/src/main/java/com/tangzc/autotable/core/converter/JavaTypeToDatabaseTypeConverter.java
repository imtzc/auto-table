package com.tangzc.autotable.core.converter;

import com.tangzc.autotable.annotation.ColumnType;
import com.tangzc.autotable.core.constants.DatabaseDialect;
import com.tangzc.autotable.core.converter.type.DefaultTypeEnumInterface;
import com.tangzc.autotable.core.converter.type.MySqlDefaultTypeEnum;
import com.tangzc.autotable.core.converter.type.PgsqlDefaultTypeEnum;
import com.tangzc.autotable.core.converter.type.SqliteDefaultTypeEnum;
import com.tangzc.autotable.core.utils.StringUtils;
import com.tangzc.autotable.core.utils.TableBeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义java转MySQL的类型转换器
 *
 * @author don
 */
public interface JavaTypeToDatabaseTypeConverter {

    Logger log = LoggerFactory.getLogger(JavaTypeToDatabaseTypeConverter.class);

    Map<String, Map<Class<?>, DefaultTypeEnumInterface>> JAVA_TO_DB_TYPE_MAP = new HashMap<String, Map<Class<?>, DefaultTypeEnumInterface>>() {{
        put(DatabaseDialect.MySQL, new HashMap<Class<?>, DefaultTypeEnumInterface>() {{
            put(String.class, MySqlDefaultTypeEnum.VARCHAR);
            put(Character.class, MySqlDefaultTypeEnum.CHAR);
            put(char.class, MySqlDefaultTypeEnum.CHAR);

            put(BigInteger.class, MySqlDefaultTypeEnum.BIGINT);
            put(Long.class, MySqlDefaultTypeEnum.BIGINT);
            put(long.class, MySqlDefaultTypeEnum.BIGINT);

            put(Integer.class, MySqlDefaultTypeEnum.INT);
            put(int.class, MySqlDefaultTypeEnum.INT);

            put(Boolean.class, MySqlDefaultTypeEnum.BIT);
            put(boolean.class, MySqlDefaultTypeEnum.BIT);

            put(Float.class, MySqlDefaultTypeEnum.FLOAT);
            put(float.class, MySqlDefaultTypeEnum.FLOAT);
            put(Double.class, MySqlDefaultTypeEnum.DOUBLE);
            put(double.class, MySqlDefaultTypeEnum.DOUBLE);
            put(BigDecimal.class, MySqlDefaultTypeEnum.DECIMAL);

            put(Date.class, MySqlDefaultTypeEnum.DATETIME);
            put(java.sql.Date.class, MySqlDefaultTypeEnum.DATE);
            put(java.sql.Timestamp.class, MySqlDefaultTypeEnum.DATETIME);
            put(java.sql.Time.class, MySqlDefaultTypeEnum.TIME);
            put(LocalDateTime.class, MySqlDefaultTypeEnum.DATETIME);
            put(LocalDate.class, MySqlDefaultTypeEnum.DATE);
            put(LocalTime.class, MySqlDefaultTypeEnum.TIME);

            put(Short.class, MySqlDefaultTypeEnum.SMALLINT);
            put(short.class, MySqlDefaultTypeEnum.SMALLINT);
        }});

        put(DatabaseDialect.PostgreSQL, new HashMap<Class<?>, DefaultTypeEnumInterface>() {{
            put(String.class, PgsqlDefaultTypeEnum.VARCHAR);
            put(Character.class, PgsqlDefaultTypeEnum.CHAR);
            put(char.class, PgsqlDefaultTypeEnum.CHAR);

            put(BigInteger.class, PgsqlDefaultTypeEnum.INT8);
            put(Long.class, PgsqlDefaultTypeEnum.INT8);
            put(long.class, PgsqlDefaultTypeEnum.INT8);

            put(Integer.class, PgsqlDefaultTypeEnum.INT4);
            put(int.class, PgsqlDefaultTypeEnum.INT4);

            put(Boolean.class, PgsqlDefaultTypeEnum.BOOL);
            put(boolean.class, PgsqlDefaultTypeEnum.BOOL);

            put(Float.class, PgsqlDefaultTypeEnum.FLOAT4);
            put(float.class, PgsqlDefaultTypeEnum.FLOAT4);
            put(Double.class, PgsqlDefaultTypeEnum.FLOAT8);
            put(double.class, PgsqlDefaultTypeEnum.FLOAT8);
            put(BigDecimal.class, PgsqlDefaultTypeEnum.NUMERIC);

            put(Date.class, PgsqlDefaultTypeEnum.TIMESTAMP);
            put(java.sql.Date.class, PgsqlDefaultTypeEnum.TIMESTAMP);
            put(java.sql.Timestamp.class, PgsqlDefaultTypeEnum.TIMESTAMP);
            put(java.sql.Time.class, PgsqlDefaultTypeEnum.TIME);
            put(LocalDateTime.class, PgsqlDefaultTypeEnum.TIMESTAMP);
            put(LocalDate.class, PgsqlDefaultTypeEnum.DATE);
            put(LocalTime.class, PgsqlDefaultTypeEnum.TIME);

            put(Short.class, PgsqlDefaultTypeEnum.INT2);
            put(short.class, PgsqlDefaultTypeEnum.INT2);
        }});

        put(DatabaseDialect.SQLite, new HashMap<Class<?>, DefaultTypeEnumInterface>() {{
            put(String.class, SqliteDefaultTypeEnum.TEXT);
            put(Character.class, SqliteDefaultTypeEnum.TEXT);
            put(char.class, SqliteDefaultTypeEnum.TEXT);

            put(BigInteger.class, SqliteDefaultTypeEnum.INTEGER);
            put(Long.class, SqliteDefaultTypeEnum.INTEGER);
            put(long.class, SqliteDefaultTypeEnum.INTEGER);

            put(Integer.class, SqliteDefaultTypeEnum.INTEGER);
            put(int.class, SqliteDefaultTypeEnum.INTEGER);

            put(Boolean.class, SqliteDefaultTypeEnum.INTEGER);
            put(boolean.class, SqliteDefaultTypeEnum.INTEGER);

            put(Float.class, SqliteDefaultTypeEnum.REAL);
            put(float.class, SqliteDefaultTypeEnum.REAL);
            put(Double.class, SqliteDefaultTypeEnum.REAL);
            put(double.class, SqliteDefaultTypeEnum.REAL);
            put(BigDecimal.class, SqliteDefaultTypeEnum.REAL);

            put(Date.class, SqliteDefaultTypeEnum.TEXT);
            put(java.sql.Date.class, SqliteDefaultTypeEnum.TEXT);
            put(java.sql.Timestamp.class, SqliteDefaultTypeEnum.TEXT);
            put(java.sql.Time.class, SqliteDefaultTypeEnum.TEXT);
            put(LocalDateTime.class, SqliteDefaultTypeEnum.TEXT);
            put(LocalDate.class, SqliteDefaultTypeEnum.TEXT);
            put(LocalTime.class, SqliteDefaultTypeEnum.TEXT);

            put(Short.class, SqliteDefaultTypeEnum.INTEGER);
            put(short.class, SqliteDefaultTypeEnum.INTEGER);
        }});
    }};

    /**
     * 添加类型映射
     *
     * @param databaseDialect 数据库类型，参考{@link DatabaseDialect}中的常量
     * @param clazz           字段类型
     * @param typeEnum        数据库类型
     */
    static void addTypeMap(String databaseDialect, Class<?> clazz, DefaultTypeEnumInterface typeEnum) {
        if (JAVA_TO_DB_TYPE_MAP.containsKey(databaseDialect)) {
            JAVA_TO_DB_TYPE_MAP.get(databaseDialect).put(clazz, typeEnum);
        }
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

        Map<Class<?>, DefaultTypeEnumInterface> typeMap = JAVA_TO_DB_TYPE_MAP.getOrDefault(databaseDialect, Collections.emptyMap());
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
