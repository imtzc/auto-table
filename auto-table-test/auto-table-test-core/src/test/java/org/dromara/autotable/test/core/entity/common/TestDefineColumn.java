package org.dromara.autotable.test.core.entity.common;

import lombok.Data;
import org.dromara.autotable.annotation.AutoColumn;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnComment;
import org.dromara.autotable.annotation.ColumnDefault;
import org.dromara.autotable.annotation.ColumnNotNull;
import org.dromara.autotable.annotation.ColumnType;
import org.dromara.autotable.annotation.Ignore;
import org.dromara.autotable.annotation.enums.DefaultValueEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AutoTable
public class TestDefineColumn {

    @ColumnComment("id主键")
    protected String id;

    @ColumnNotNull
    @ColumnDefault(type = DefaultValueEnum.EMPTY_STRING)
    @ColumnType(length = 100)
    @ColumnComment("用户名")
    protected String username;

    @ColumnDefault("0")
    @ColumnComment("年龄")
    protected Integer age;

    @ColumnType(length = 20)
    @AutoColumn(comment = "电话", defaultValue = "+00 00000000", notNull = true)
    protected String phone;

    @AutoColumn(comment = "资产", length = 12, decimalLength = 6)
    protected BigDecimal money;

    @ColumnDefault("true")
    @AutoColumn(comment = "激活状态")
    protected Boolean active;

    @ColumnComment("个人简介")
    protected String description;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @AutoColumn(comment = "注册时间")
    protected LocalDateTime registerTime;

    @Ignore
    protected String extra;
}
