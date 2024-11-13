package com.tangzc.autotable.core.converter;

/**
 * 数据库数据类型接口
 * @author don
 */
public interface DefaultTypeEnumInterface {
    /**
     * 默认类型长度
     * @return 默认长度
     */
    Integer getDefaultLength();
    /**
     * 默认小数点后长度
     * @return 默认小数点后长度
     */
    Integer getDefaultDecimalLength();
    /**
     * 类型名称
     * @return 类型名称
     */
    String getTypeName();
}
