package org.dromara.autotable.test.core.entity.mysql;

import lombok.Data;
import org.dromara.autotable.annotation.AutoTable;
import org.dromara.autotable.annotation.mysql.MysqlColumnCharset;

@Data
@AutoTable
public class TestColumnCharset {
    @MysqlColumnCharset(value = "utf8mb4", collate = "utf8mb4_general_ci")
    private String testColumn;
}
