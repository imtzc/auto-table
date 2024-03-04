package com.tangzc.autotable.test.springboot;

import com.tangzc.autotable.annotation.ColumnComment;
import com.tangzc.autotable.annotation.ColumnType;
import com.tangzc.autotable.annotation.PrimaryKey;
import com.tangzc.autotable.annotation.TableName;
import com.tangzc.autotable.annotation.mysql.MysqlTypeConstant;
import lombok.Data;

@Data
@TableName("sys_user")
public class User {

    @PrimaryKey(true)
    @ColumnComment("用户id")
    private Long id;

    @ColumnComment("电话")
    private String phone;

    @ColumnComment("备注")
    @ColumnType(MysqlTypeConstant.TEXT)
    private String mark;
}
