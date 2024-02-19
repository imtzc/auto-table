package com.tangzc.autotable.core.strategy.sqlite;

import com.tangzc.autotable.core.converter.DatabaseTypeAndLength;
import com.tangzc.autotable.core.converter.type.SqliteDefaultTypeEnum;

/**
 * @author don
 */
public class SqliteTypeHelper {

    public static String getFullType(DatabaseTypeAndLength databaseTypeAndLength) {
        // 例：double(4,2) unsigned zerofill
        String typeAndLength = databaseTypeAndLength.getType();
        // 类型具备长度属性 且 自定义长度不为空
        Integer length = databaseTypeAndLength.getLength();
        if (length != null && length > 0) {
            typeAndLength += "(" + length;
            Integer decimalLength = databaseTypeAndLength.getDecimalLength();
            if (decimalLength != null && decimalLength > 0) {
                typeAndLength += "," + decimalLength;
            }
            typeAndLength += ")";
        }

        return typeAndLength;
    }

    public static boolean isText(DatabaseTypeAndLength type) {
        return SqliteDefaultTypeEnum.TEXT.getTypeName().equalsIgnoreCase(type.getType());
    }

    public static boolean isInteger(DatabaseTypeAndLength type) {
        return SqliteDefaultTypeEnum.INTEGER.getTypeName().equalsIgnoreCase(type.getType());
    }
}
