package com.tangzc.autotable.test.core.dynamicdatasource;

import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.autotable.annotation.ColumnComment;
import com.tangzc.autotable.annotation.ColumnDefault;
import com.tangzc.autotable.annotation.ColumnType;
import com.tangzc.autotable.annotation.Index;
import com.tangzc.autotable.annotation.PrimaryKey;
import com.tangzc.autotable.annotation.enums.DefaultValueEnum;
import com.tangzc.autotable.annotation.mysql.MysqlColumnCharset;
import com.tangzc.autotable.annotation.mysql.MysqlTypeConstant;
import lombok.Data;

/**
 * @author don
 */
@Data
@Ds("pgsql")
@AutoTable(value = "sys_user", schema = "my_schema", comment = "用户表")
public class UserPgsql {

    @PrimaryKey(true)
    @ColumnComment("用户id")
    private Long id;

    @ColumnComment("姓名")
    @Index
    private String name;

    @ColumnComment("备注")
    @ColumnType(MysqlTypeConstant.TEXT)
    @ColumnDefault(type = DefaultValueEnum.NULL)
    private String mark;
}
