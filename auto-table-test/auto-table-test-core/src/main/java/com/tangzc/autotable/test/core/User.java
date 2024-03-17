package com.tangzc.autotable.test.core;

import com.tangzc.autotable.annotation.ColumnComment;
import com.tangzc.autotable.annotation.ColumnDefault;
import com.tangzc.autotable.annotation.ColumnType;
import com.tangzc.autotable.annotation.PrimaryKey;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.autotable.annotation.TableIndexes;
import com.tangzc.autotable.annotation.TableName;
import com.tangzc.autotable.annotation.enums.DefaultValueEnum;
import com.tangzc.autotable.annotation.enums.IndexTypeEnum;
import com.tangzc.autotable.annotation.mysql.MysqlColumnCharset;
import com.tangzc.autotable.annotation.mysql.MysqlTypeConstant;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@TableName("sys_user")
@TableIndexes({
        @TableIndex(name = "uni_name", fields = User.Fields.name, type = IndexTypeEnum.UNIQUE, comment = "姓名唯一索引"),
        @TableIndex(name = "uni_age", fields = User.Fields.age, type = IndexTypeEnum.NORMAL, comment = "年龄普通索引")
})
@FieldNameConstants
public class User extends BaseEntity {

    @PrimaryKey(true)
    @ColumnComment("用户id")
    private Long id;

    @ColumnComment("姓名")
    private String name;

    @ColumnComment("年龄")
    private Integer age;

    @ColumnComment("备注")
    @ColumnType(MysqlTypeConstant.TEXT)
    @ColumnDefault(type = DefaultValueEnum.NULL)
    @MysqlColumnCharset(value = "utf8mb4", collate = "utf8mb4_0900_ai_ci")
    private String mark;
}
