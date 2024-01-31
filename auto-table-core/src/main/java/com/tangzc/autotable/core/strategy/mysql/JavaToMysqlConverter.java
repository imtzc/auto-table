package com.tangzc.autotable.core.strategy.mysql;

import com.tangzc.autotable.core.strategy.mysql.data.MysqlTypeAndLength;
import com.tangzc.autotable.core.strategy.mysql.data.enums.MySqlDefaultTypeEnum;
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
 * 自定义java转MySQL的类型转换器
 * @author don
 */
public interface JavaToMysqlConverter {

    Map<Class<?>, MySqlDefaultTypeEnum> JAVA_TO_MYSQL_TYPE_MAP = new HashMap<Class<?>, MySqlDefaultTypeEnum>() {{
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
    }};

    /**
     * java转pgsql类型
     */
    default MysqlTypeAndLength convert(Class<?> clazz, Field field) {

        Class<?> fieldClass = TableBeanUtils.getFieldType(clazz, field);

        MySqlDefaultTypeEnum sqlType = JAVA_TO_MYSQL_TYPE_MAP.get(fieldClass);

        if (sqlType == null) {
            throw new RuntimeException(fieldClass + "默认情况下，不支持转换到mysql类型，如有需要请自行实现" + JavaToMysqlConverter.class.getName());
        }

        return new MysqlTypeAndLength(sqlType.getLengthDefault(), sqlType.getDecimalLengthDefault(), sqlType.name().toLowerCase());
    }
}
