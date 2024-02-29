package com.tangzc.autotable.core.strategy.pgsql.data;

import com.tangzc.autotable.core.converter.DatabaseTypeAndLength;

/**
 * @author don
 */
public class PgsqlTypeHelper {

    public static String getFullType(DatabaseTypeAndLength databaseTypeAndLength) {
        String type = databaseTypeAndLength.getType();
        // 类型具备长度属性 且 自定义长度不为空
        Integer length = databaseTypeAndLength.getLength();
        Integer decimalLength = databaseTypeAndLength.getDecimalLength();
        if (length != null) {
            type += "(" + length;
            if (decimalLength != null) {
                type += "," + decimalLength;
            }
            type += ")";
        }

        return type;
    }

    public static boolean isCharString(DatabaseTypeAndLength databaseTypeAndLength) {
        String type = databaseTypeAndLength.getType();
        return PgsqlDefaultTypeEnum.CHAR.getTypeName().equalsIgnoreCase(type) ||
                PgsqlDefaultTypeEnum.VARCHAR.getTypeName().equalsIgnoreCase(type) ||
                PgsqlDefaultTypeEnum.TEXT.getTypeName().equalsIgnoreCase(type);
    }

    public static boolean isBoolean(DatabaseTypeAndLength databaseTypeAndLength) {
        return PgsqlDefaultTypeEnum.BOOL.getTypeName().equalsIgnoreCase(databaseTypeAndLength.getType());
    }

    public static boolean isTime(DatabaseTypeAndLength databaseTypeAndLength) {
        String type = databaseTypeAndLength.getType();
        return PgsqlDefaultTypeEnum.DATE.getTypeName().equalsIgnoreCase(type)
                || PgsqlDefaultTypeEnum.TIMESTAMP.getTypeName().equalsIgnoreCase(type)
                || PgsqlDefaultTypeEnum.TIME.getTypeName().equalsIgnoreCase(type);
    }
}
