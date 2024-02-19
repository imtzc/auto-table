package com.tangzc.autotable.core.converter.type;

public interface DefaultTypeEnumInterface {
    /**
     * 默认类型长度
     */
    Integer getDefaultLength();
    /**
     * 默认小数点后长度
     */
    Integer getDefaultDecimalLength();
    /**
     * 类型名称
     */
    String getTypeName();
}
