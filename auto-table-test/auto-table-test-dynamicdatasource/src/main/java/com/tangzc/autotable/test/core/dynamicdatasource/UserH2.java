package com.tangzc.autotable.test.core.dynamicdatasource;

import com.tangzc.autotable.annotation.AutoColumn;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.autotable.annotation.ColumnComment;
import com.tangzc.autotable.annotation.ColumnType;
import com.tangzc.autotable.annotation.PrimaryKey;
import com.tangzc.autotable.annotation.enums.DefaultValueEnum;
import com.tangzc.autotable.annotation.mysql.MysqlTypeConstant;
import lombok.Data;

@Data
@Ds("h2")
@AutoTable(value = "sys_user", schema = "my_test")
public class UserH2 {

    @PrimaryKey(true)
    @ColumnComment("用户id")
    private Long id;

    @ColumnComment("姓名")
    @ColumnType(value = "text", length = 100)
    private String name;

    // @ColumnComment("备注")
    // @ColumnType(MysqlTypeConstant.TEXT)
    // @ColumnDefault(type = DefaultValueEnum.NULL)
    @AutoColumn(type = MysqlTypeConstant.TEXT, defaultValueType = DefaultValueEnum.NULL, comment = "备注")
    private String mark;
}
