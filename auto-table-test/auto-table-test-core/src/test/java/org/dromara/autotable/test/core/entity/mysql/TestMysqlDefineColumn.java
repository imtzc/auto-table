package org.dromara.autotable.test.core.entity.mysql;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnComment;
import org.dromara.autotable.annotation.ColumnType;
import org.dromara.autotable.annotation.mysql.MysqlTypeConstant;
import org.dromara.autotable.test.core.entity.common.TestDefineColumn;

@EqualsAndHashCode(callSuper = true)
@Data
@AutoTable
public class TestMysqlDefineColumn extends TestDefineColumn {

    // 指定主键自增注释、类型（数据库数字类型可以跟java字符串类型相互转化）、长度
    // 注意字段名称id会被自动认定为主键不需要再额外指定
    @ColumnComment("id主键")
    @ColumnType(value = MysqlTypeConstant.BIGINT, length = 32)
    private String id;

    // 单独设置字段类型
    @ColumnType(MysqlTypeConstant.TEXT)
    @ColumnComment("个人简介")
    private String description;
}
