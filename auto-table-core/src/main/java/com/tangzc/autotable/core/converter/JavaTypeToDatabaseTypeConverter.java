package com.tangzc.autotable.core.converter;

import com.tangzc.autotable.annotation.ColumnType;
import com.tangzc.autotable.core.constants.DatabaseDialect;
import com.tangzc.autotable.core.converter.type.DefaultTypeEnumInterface;
import com.tangzc.autotable.core.converter.type.PgsqlDefaultTypeEnum;
import com.tangzc.autotable.core.converter.type.SqliteDefaultTypeEnum;
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
import java.util.Map;

/**
 * 自定义java转MySQL的类型转换器
 *
 * @author don
 */
public interface JavaTypeToDatabaseTypeConverter {

    Logger log = LoggerFactory.getLogger(JavaTypeToDatabaseTypeConverter.class);

    Map<String, Map<Class<?>, ? extends DefaultTypeEnumInterface>> JAVA_TO_DB_TYPE_MAP = new HashMap<String, Map<Class<?>, ? extends DefaultTypeEnumInterface>>(){{
        put(DatabaseDialect.MYSQL, new HashMap<Class<?>, PgsqlDefaultTypeEnum>() {{
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

        put(DatabaseDialect.POSTGRESQL, new HashMap<Class<?>, PgsqlDefaultTypeEnum>() {{
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

        put(DatabaseDialect.SQLITE, new HashMap<Class<?>, SqliteDefaultTypeEnum>() {{
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
     * java转数据库类型
     */
    default DatabaseTypeAndLength convert(String databaseDialect, Class<?> clazz, Field field) {

        DatabaseTypeAndLength typeAndLength;

        ColumnType column = TableBeanUtils.getColumnType(field);
        if (column != null) {
            // 如果设置了类型
            int length = column.length();
            int decimalLength = column.decimalLength();
            typeAndLength = new DatabaseTypeAndLength(column.value(), length > -1 ? length : null, decimalLength > -1 ? decimalLength : null, Arrays.asList(column.values()));
        } else {
            typeAndLength = getDatabaseTypeAndLength(databaseDialect, clazz, field);
        }

        return typeAndLength;
    }

    default DatabaseTypeAndLength getDatabaseTypeAndLength(String databaseDialect, Class<?> clazz, Field field) {

        DatabaseTypeAndLength typeAndLength;
        Class<?> fieldClass = TableBeanUtils.getFieldType(clazz, field);

        Map<Class<?>, ? extends DefaultTypeEnumInterface> typeMap = JAVA_TO_DB_TYPE_MAP.getOrDefault(databaseDialect, Collections.emptyMap());
        if (typeMap.isEmpty()) {
            log.warn("数据库方言" + databaseDialect + "没有找到对应的数据库类型映射关系");
        }

        DefaultTypeEnumInterface sqlType = typeMap.get(fieldClass);

        if (sqlType == null) {
            throw new RuntimeException("字段" + fieldClass + "找不到" + databaseDialect + "对应的类型，请自行实现" + JavaTypeToDatabaseTypeConverter.class.getName());
        }
        typeAndLength = new DatabaseTypeAndLength(sqlType.getTypeName(), sqlType.getDefaultLength(), sqlType.getDefaultDecimalLength(), Collections.emptyList());
        return typeAndLength;
    }
}
