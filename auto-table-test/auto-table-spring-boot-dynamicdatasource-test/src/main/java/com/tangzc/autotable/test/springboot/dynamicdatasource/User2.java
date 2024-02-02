package com.tangzc.autotable.test.springboot.dynamicdatasource;

import com.tangzc.autotable.annotation.ColumnComment;
import com.tangzc.autotable.annotation.ColumnDefault;
import com.tangzc.autotable.annotation.ColumnType;
import com.tangzc.autotable.annotation.PrimaryKey;
import com.tangzc.autotable.annotation.TableName;
import com.tangzc.autotable.annotation.enums.DefaultValueEnum;
import com.tangzc.autotable.annotation.mysql.MysqlColumnCharset;
import com.tangzc.autotable.annotation.mysql.MysqlTypeConstant;
import com.tangzc.autotable.test.springboot.dynamicdatasource.dynamicdatasourceframe.DataSourceConstants;
import com.tangzc.autotable.test.springboot.dynamicdatasource.dynamicdatasourceframe.Ds;
import lombok.Data;

@Data
@Ds(DataSourceConstants.DS_KEY_SLAVE)
@TableName("sys_user2")
public class User2 {

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
