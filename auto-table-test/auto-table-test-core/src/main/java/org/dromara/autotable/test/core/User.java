package org.dromara.autotable.test.core;

import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnComment;
import org.dromara.autotable.annotation.ColumnDefault;
import org.dromara.autotable.annotation.ColumnType;
import org.dromara.autotable.annotation.IndexField;
import org.dromara.autotable.annotation.PrimaryKey;
import org.dromara.autotable.annotation.TableIndex;
import org.dromara.autotable.annotation.TableIndexes;
import org.dromara.autotable.annotation.enums.DefaultValueEnum;
import org.dromara.autotable.annotation.enums.IndexSortTypeEnum;
import org.dromara.autotable.annotation.enums.IndexTypeEnum;
import org.dromara.autotable.annotation.mysql.MysqlColumnCharset;
import org.dromara.autotable.annotation.mysql.MysqlColumnUnsigned;
import org.dromara.autotable.annotation.mysql.MysqlColumnZerofill;
import org.dromara.autotable.annotation.mysql.MysqlTypeConstant;
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
