package com.tangzc.autotable.annotation.h2;

/**
 * @author don
 */
public interface H2TypeConstant {

    /**
     * 整数
     */
    String INT = "int";
    String TINYINT = "tinyint";
    String SMARTINT = "smartint";
    String BIGINT = "bigint";
    String IDENTITY = "identity";
    /**
     * 小数
     */
    String REAL = "real";
    String DOUBLE = "double";
    String DECIMAL = "decimal";
    /**
     * 字符串
     */
    String CHAR = "char";
    String VARCHAR = "varchar";
    String VARCHAR_IGNORECASE = "varchar_ignorecase";
    String UUID = "uuid";
    String CLOB = "clob";
    /**
     * 日期
     */
    String TIME = "time";
    String DATE = "date";
    String TIMESTAMP = "timestamp";
    /**
     * 二进制
     */
    String BINARY = "binary";
    String BLOB = "blob";
    /**
     * 布尔
     */
    String BOOLEAN = "boolean";
    /**
     * 其他
     */
    String OTHER = "other";
    String ARRAY = "array";
}
