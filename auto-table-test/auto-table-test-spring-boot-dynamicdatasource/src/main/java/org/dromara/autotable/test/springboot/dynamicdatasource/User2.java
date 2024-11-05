package org.dromara.autotable.test.springboot.dynamicdatasource;

import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnComment;
import org.dromara.autotable.annotation.ColumnDefault;
import org.dromara.autotable.annotation.ColumnType;
import org.dromara.autotable.annotation.PrimaryKey;
import org.dromara.autotable.annotation.enums.DefaultValueEnum;
import org.dromara.autotable.annotation.mysql.MysqlColumnCharset;
import org.dromara.autotable.annotation.mysql.MysqlTypeConstant;
import org.dromara.autotable.test.springboot.dynamicdatasource.dynamicdatasourceframe.DataSourceConstants;
import org.dromara.autotable.test.springboot.dynamicdatasource.dynamicdatasourceframe.Ds;
import lombok.Data;

@Data
@Ds(DataSourceConstants.DS_KEY_SLAVE)
@AutoTable("sys_user2")
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
