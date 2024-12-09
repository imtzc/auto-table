package org.dromara.autotable.test.core.entity.mysql;

import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.ColumnComment;
import org.dromara.autotable.annotation.ColumnType;
import org.dromara.autotable.annotation.mysql.MysqlColumnZerofill;

/**
 * @author don
 */
@Data
@AutoTable
public class TestColumnZerofill {

    @ColumnComment("电话")
    @MysqlColumnZerofill
    @ColumnType(length = 13)
    private Integer phone;
}
