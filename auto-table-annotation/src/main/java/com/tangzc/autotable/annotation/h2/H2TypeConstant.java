package com.tangzc.autotable.annotation.h2;

/**
 * @author don
 */
public interface H2TypeConstant {

    /**
     * 整数
     */
    String INTEGER = "INTEGER";
    String TINYINT = "TINYINT";
    String SMARTINT = "SMARTINT";
    String BIGINT = "BIGINT";
    /**
     * 小数
     */
    String NUMERIC = "NUMERIC";
    String REAL = "REAL";
    /**
     * 字符串
     */
    String CHARACTER = "CHARACTER";
    String CHARACTER_VARYING = "CHARACTER VARYING";
    String VARCHAR_IGNORECASE = "VARCHAR_IGNORECASE";
    String UUID = "UUID";
    String CHARACTER_LARGE_OBJECT = "CHARACTER LARGE OBJECT";
    /**
     * 日期
     */
    String TIME = "TIME";
    String DATE = "DATE";
    String TIMESTAMP = "TIMESTAMP";
    /**
     * 二进制
     */
    String BINARY = "BINARY";
    String BLOB = "BLOB";
    /**
     * 布尔
     */
    String BOOLEAN = "BOOLEAN";
    /**
     * 其他
     */
    String OTHER = "OTHER";
    String ARRAY = "ARRAY";
}
