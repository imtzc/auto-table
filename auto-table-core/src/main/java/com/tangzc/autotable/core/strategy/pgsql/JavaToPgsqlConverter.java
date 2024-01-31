package com.tangzc.autotable.core.strategy.pgsql;

import com.tangzc.autotable.core.strategy.pgsql.data.PgsqlTypeAndLength;
import com.tangzc.autotable.core.strategy.pgsql.data.enums.PgsqlDefaultTypeEnum;
import com.tangzc.autotable.core.utils.TableBeanUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author don
 */
public interface JavaToPgsqlConverter {
    Map<Class<?>, PgsqlDefaultTypeEnum> JAVA_TO_PGSQL_TYPE_MAP = new HashMap<Class<?>, PgsqlDefaultTypeEnum>() {{
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
    }};

    /**
     * java转pgsql类型
     */
    default PgsqlTypeAndLength convert(Class<?> clazz, Field field) {

        Class<?> fieldClass = TableBeanUtils.getFieldType(clazz, field);
        PgsqlDefaultTypeEnum sqlType = JAVA_TO_PGSQL_TYPE_MAP.getOrDefault(fieldClass, PgsqlDefaultTypeEnum.VARCHAR);

        if (sqlType == null) {
            throw new RuntimeException(fieldClass + "默认情况下，不支持转换到pgsql类型，如有需要请自行实现" + JavaToPgsqlConverter.class.getName());
        }
        return new PgsqlTypeAndLength(sqlType.getLengthDefault(), sqlType.getDecimalLengthDefault(), sqlType.name().toLowerCase());
    }
}
