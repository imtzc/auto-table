package com.tangzc.autotable.test.core;

import com.tangzc.autotable.annotation.AutoTable;
import com.tangzc.autotable.annotation.ColumnComment;
import com.tangzc.autotable.annotation.ColumnDefault;
import com.tangzc.autotable.annotation.ColumnType;
import com.tangzc.autotable.annotation.IndexField;
import com.tangzc.autotable.annotation.PrimaryKey;
import com.tangzc.autotable.annotation.TableIndex;
import com.tangzc.autotable.annotation.TableIndexes;
import com.tangzc.autotable.annotation.enums.DefaultValueEnum;
import com.tangzc.autotable.annotation.enums.IndexSortTypeEnum;
import com.tangzc.autotable.annotation.enums.IndexTypeEnum;
import com.tangzc.autotable.annotation.mysql.MysqlColumnCharset;
import com.tangzc.autotable.annotation.mysql.MysqlColumnUnsigned;
import com.tangzc.autotable.annotation.mysql.MysqlColumnZerofill;
import com.tangzc.autotable.annotation.mysql.MysqlTypeConstant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

/**
 * @author don
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AutoTable("sys_user")
@TableIndexes({
        @TableIndex(fields = User.Fields.name, type = IndexTypeEnum.UNIQUE, comment = "姓名唯一索引"),
        @TableIndex(indexFields = @IndexField(field = User.Fields.age, sort = IndexSortTypeEnum.DESC), type = IndexTypeEnum.NORMAL, comment = "年龄普通索引")
})
@FieldNameConstants
public class User extends FatherEntity<String> {

    @PrimaryKey(true)
    @ColumnComment("用户id")
    private Long id;

    @ColumnComment("姓名")
    private String name;

    @ColumnComment("年龄")
    @MysqlColumnUnsigned
    private Integer age;

    @ColumnComment("电话")
    @MysqlColumnZerofill
    @ColumnType(length = 13)
    private Integer phone;

    @ColumnComment("备注")
    @ColumnType(MysqlTypeConstant.TEXT)
    @ColumnDefault(type = DefaultValueEnum.NULL)
    @MysqlColumnCharset(value = "utf8mb4", collate = "utf8mb4_0900_ai_ci")
    private String mark;
}
