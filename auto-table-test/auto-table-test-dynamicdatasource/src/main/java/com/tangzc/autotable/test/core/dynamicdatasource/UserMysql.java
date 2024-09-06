package com.tangzc.autotable.test.core.dynamicdatasource;

import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.autotable.annotation.ColumnComment;
import com.tangzc.autotable.annotation.ColumnDefault;
import com.tangzc.autotable.annotation.ColumnType;
import com.tangzc.autotable.annotation.PrimaryKey;
import com.tangzc.autotable.annotation.enums.DefaultValueEnum;
import com.tangzc.autotable.annotation.mysql.MysqlColumnCharset;
import com.tangzc.autotable.annotation.mysql.MysqlTypeConstant;
import lombok.Data;

/**
 * @author don
 */
@Data
@AutoTable(value = "sys_user", comment = "用户表")
public class UserMysql {

    @PrimaryKey(true)
    @ColumnComment("用户id")
    private Long id;

    @ColumnComment("姓名")
    private String name;

    @ColumnComment("备注")
    @ColumnType(MysqlTypeConstant.TEXT)
    @ColumnDefault(type = DefaultValueEnum.NULL)
    @MysqlColumnCharset(value = "utf8mb4", collate = "utf8mb4_0900_ai_ci")
    private String mark;
}
