package com.tangzc.autotable.core.strategy.mysql.data;

import com.tangzc.autotable.core.strategy.mysql.data.enums.MySqlDefaultTypeEnum;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author don
 */
@Data
@NoArgsConstructor
public class MysqlTypeAndLength {
    /**
     * 长度
     */
    private Integer length;
    /**
     * 小数位数
     */
    private Integer decimalLength;
    /**
     * 类型
     */
    private String type;
    /**
     * enum、set的值数组
     */
    private List<String> values;

    public MysqlTypeAndLength(Integer length, Integer decimalLength, String type) {
        if (length != null && length >= 0) {
            this.length = length;
        }
        if (decimalLength != null && decimalLength >= 0) {
            this.decimalLength = decimalLength;
        }
        this.type = type;
        this.values = Collections.emptyList();
    }

    public static final Set<String> CHAR_STRING_TYPE = new HashSet<>(Arrays.asList(
            MySqlDefaultTypeEnum.CHAR.typeName(),
            MySqlDefaultTypeEnum.VARCHAR.typeName(),
            MySqlDefaultTypeEnum.TEXT.typeName(),
            MySqlDefaultTypeEnum.TINYTEXT.typeName(),
            MySqlDefaultTypeEnum.MEDIUMTEXT.typeName(),
            MySqlDefaultTypeEnum.LONGTEXT.typeName(),
            MySqlDefaultTypeEnum.ENUM.typeName(),
            MySqlDefaultTypeEnum.SET.typeName()
    ));

    public static final Set<String> ENUM_OR_SET_TYPE = new HashSet<>(Arrays.asList(
            MySqlDefaultTypeEnum.ENUM.typeName(),
            MySqlDefaultTypeEnum.SET.typeName()
    ));

    public static final Set<String> DATE_TIME_TYPE = new HashSet<>(Arrays.asList(
            MySqlDefaultTypeEnum.DATE.typeName(),
            MySqlDefaultTypeEnum.DATETIME.typeName(),
            MySqlDefaultTypeEnum.YEAR.typeName(),
            MySqlDefaultTypeEnum.TIME.typeName()
    ));

    public static final Set<String> INTEGER_TYPE = new HashSet<>(Arrays.asList(
            MySqlDefaultTypeEnum.INT.typeName(),
            MySqlDefaultTypeEnum.TINYINT.typeName(),
            MySqlDefaultTypeEnum.SMALLINT.typeName(),
            MySqlDefaultTypeEnum.MEDIUMINT.typeName(),
            MySqlDefaultTypeEnum.BIGINT.typeName()
    ));

    public static final Set<String> FLOAT_TYPE = new HashSet<>(Arrays.asList(
            MySqlDefaultTypeEnum.FLOAT.typeName(),
            MySqlDefaultTypeEnum.DOUBLE.typeName(),
            MySqlDefaultTypeEnum.DECIMAL.typeName()
    ));

    public String typeName() {
        return this.type.toLowerCase();
    }

    public String getFullType() {
        // 例：double(4,2) unsigned zerofill
        String typeAndLength = this.typeName();
        // 类型具备长度属性 且 自定义长度不为空

        if (this.isEnum()) {
            typeAndLength += "('" + String.join("','", this.values) + "')";
        } else if (this.length != null) {
            typeAndLength += "(" + this.length;
            if (this.decimalLength != null) {
                typeAndLength += "," + this.decimalLength;
            }
            typeAndLength += ")";
        }

        return typeAndLength;
    }

    public boolean isCharString() {
        return CHAR_STRING_TYPE.contains(this.typeName());
    }

    public boolean isDateTime() {
        return DATE_TIME_TYPE.contains(this.typeName());
    }

    public boolean needStringCompatibility() {
        return isCharString() || isDateTime();
    }

    public boolean isBoolean() {
        return MySqlDefaultTypeEnum.BIT.typeName().equalsIgnoreCase(this.typeName());
    }

    public boolean isNumber() {
        return (INTEGER_TYPE.contains(this.typeName()) || FLOAT_TYPE.contains(this.typeName()));
    }

    public boolean isEnum() {
        return ENUM_OR_SET_TYPE.contains(this.typeName());
    }

    public boolean isFloatNumber() {
        return FLOAT_TYPE.contains(this.typeName());
    }

    public boolean isNoLengthNumber() {
        return INTEGER_TYPE.contains(this.typeName());
    }
}
