package com.tangzc.autotable.test.core.dynamicdatasource;

import com.tangzc.autotable.annotation.AutoColumn;
import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.autotable.annotation.ColumnComment;
import com.tangzc.autotable.annotation.ColumnType;
import com.tangzc.autotable.annotation.Index;
import com.tangzc.autotable.annotation.PrimaryKey;
import com.tangzc.autotable.annotation.enums.DefaultValueEnum;
import com.tangzc.autotable.annotation.enums.IndexTypeEnum;
import com.tangzc.autotable.annotation.h2.H2TypeConstant;
import com.tangzc.autotable.annotation.mysql.MysqlTypeConstant;
import lombok.Data;
import org.checkerframework.common.aliasing.qual.Unique;

@Data
@Ds("h2")
@AutoTable(value = "sys_user", schema = "my_test", comment = "用户表")
public class UserH2 {

    @PrimaryKey(true)
    @ColumnComment("用户id")
    private Long id;

    @Index
    @ColumnComment("姓名")
    @ColumnType(value = H2TypeConstant.CHARACTER_VARYING, length = 100)
    private String name;

    @Index(type = IndexTypeEnum.UNIQUE)
    @ColumnComment("电话")
    @ColumnType(value = H2TypeConstant.CHARACTER_VARYING, length = 11)
    private String phone;

    @AutoColumn(type = H2TypeConstant.CHARACTER_VARYING, defaultValueType = DefaultValueEnum.NULL, comment = "备注")
    private String mark;
}
