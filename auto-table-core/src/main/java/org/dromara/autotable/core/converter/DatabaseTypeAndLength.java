package org.dromara.autotable.core.converter;

import lombok.Data;

import java.util.List;

/**
 * 统一的数据类型描述对象
 *
 * @author don
 */
@Data
public class DatabaseTypeAndLength {
    /**
     * 类型
     */
    private String type;
    /**
     * 长度
     */
    private Integer length;
    /**
     * 小数位数
     */
    private Integer decimalLength;
    /**
     * enum、set的值数组
     */
    private List<String> values;

    public DatabaseTypeAndLength(String type, Integer length, Integer decimalLength, List<String> values) {
        this.type = type;
        if (length != null && length >= 0) {
            this.length = length;
        }
        if (decimalLength != null && decimalLength >= 0) {
            this.decimalLength = decimalLength;
        }
        this.values = values;
    }

    public String getDefaultFullType() {

        String fullType = type;
        if (length != null) {
            fullType += "(" + length;
            if (decimalLength != null) {
                fullType += "," + decimalLength;
            }
            fullType += ")";
        }

        return fullType;
    }
}
