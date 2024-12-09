package org.dromara.autotable.test.core.entity.mysql;

import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnComment;
import org.dromara.autotable.annotation.mysql.MysqlColumnUnsigned;

/**
 * @author don
 */
@Data
@AutoTable
public class TestColumnUnsigned {

    @ColumnComment("年龄")
    @MysqlColumnUnsigned
    private Integer age;
}
