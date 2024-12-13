package org.dromara.autotable.test.core.entity.common;

import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.core.converter.DefaultTypeEnumInterface;
import org.dromara.autotable.core.strategy.IStrategy;
import org.dromara.autotable.core.converter.JavaTypeToDatabaseTypeConverter;

import java.util.Map;

/**
 * <p>各个数据库的基础映射关系，请参照不同数据库的策略实现 {@link IStrategy#typeMapping()}</p>
 * <p>如果某个数据库没有对应的类型，可以自定义映射，请参照 {@link JavaTypeToDatabaseTypeConverter#addTypeMapping(String, Map)} 或 {@link JavaTypeToDatabaseTypeConverter#addTypeMapping(String, Class, DefaultTypeEnumInterface)}</p>
 */
@Data
@AutoTable
public class TestFieldType2DbType {

    private Long longType;
    private Integer integerType;
    private String stringType;
    private Boolean booleanType;
    private Double doubleType;
    private Float floatType;
    private Byte byteType;
    private Character characterType;
    private Short shortType;
    private byte[] byteArrayType;
    private java.sql.Date sqlDateType;
    private java.util.Date utilDateType;
    private java.sql.Timestamp timestampType;
    private java.math.BigDecimal bigDecimalType;
    private java.math.BigInteger bigIntegerType;
    private java.util.UUID uuidType;
    private java.net.URL urlType;
    private java.net.URI uriType;
    private java.util.Locale localeType;
    private java.util.Currency currencyType;
    private java.util.Calendar calendarType;
    private java.time.LocalDate localDateType;
    private java.time.LocalDateTime localDateTimeType;
    private java.time.LocalTime localTimeType;
    private java.time.OffsetDateTime offsetDateTimeType;
    private java.time.OffsetTime offsetTimeType;
    private java.time.ZonedDateTime zonedDateTimeType;
}
